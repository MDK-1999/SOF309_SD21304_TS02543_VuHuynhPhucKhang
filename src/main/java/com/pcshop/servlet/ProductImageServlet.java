package com.pcshop.servlet;

import com.pcshop.dao.ProductDAO;
import com.pcshop.model.Product;
import com.pcshop.util.ProductPresentation;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

@WebServlet("/product-image")
public class ProductImageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int WIDTH = 900;
    private static final int HEIGHT = 720;
    private static final int MAX_REMOTE_BYTES = 5 * 1024 * 1024;
    private static final int MAX_CACHE_ENTRIES = 96;
    private static final long IMAGE_TTL_MILLIS = Duration.ofHours(6).toMillis();
    private static final long PLACEHOLDER_TTL_MILLIS = Duration.ofMinutes(2).toMillis();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();
    private static final Map<Integer, CachedImage> CACHE = new ConcurrentHashMap<>();
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int productId = parseInt(request.getParameter("id"));
        if (productId <= 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long now = System.currentTimeMillis();
        CachedImage image = CACHE.get(productId);
        if (image == null || image.expiresAt() <= now) {
            if (image != null) CACHE.remove(productId, image);

            Product product = productDAO.getById(productId);
            if (product == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            CachedImage resolved = resolveImage(product, ProductPresentation.imageCandidates(product));
            long ttl = resolved.placeholder() ? PLACEHOLDER_TTL_MILLIS : IMAGE_TTL_MILLIS;
            String etag = "\"gearastral-image-" + Integer.toUnsignedString(
                31 * productId + Arrays.hashCode(resolved.bytes()), 36) + "\"";
            image = new CachedImage(resolved.bytes(), resolved.contentType(),
                resolved.placeholder(), etag, now + ttl);
            trimCache(now);
            CACHE.put(productId, image);
        }

        response.setHeader("ETag", image.etag());
        response.setHeader("Cache-Control", image.placeholder()
            ? "public, max-age=60" : "public, max-age=86400, stale-while-revalidate=604800");
        response.setHeader("X-Content-Type-Options", "nosniff");
        if (image.etag().equals(request.getHeader("If-None-Match"))) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        response.setContentType(image.contentType());
        response.setContentLength(image.bytes().length);
        response.getOutputStream().write(image.bytes());
    }

    private void trimCache(long now) {
        CACHE.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
        int overflow = CACHE.size() - MAX_CACHE_ENTRIES + 1;
        if (overflow <= 0) return;
        for (Map.Entry<Integer, CachedImage> entry : CACHE.entrySet()) {
            if (overflow-- <= 0) break;
            CACHE.remove(entry.getKey(), entry.getValue());
        }
    }

    private CachedImage resolveImage(Product product, List<String> candidates) {
        for (String candidate : candidates) {
            CachedImage image = fetchImage(candidate);
            if (image != null) return image;
        }
        return new CachedImage(createPlaceholder(product), "image/png", true, null, 0);
    }

    private CachedImage fetchImage(String url) {
        try {
            URI current = URI.create(url);
            for (int redirect = 0; redirect <= 3; redirect++) {
                if (!isPublicHttpUri(current)) return null;

                String referer = current.getScheme() + "://" + current.getAuthority() + "/";
                HttpRequest remoteRequest = HttpRequest.newBuilder(current)
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/126 Safari/537.36")
                    .header("Accept", "image/avif,image/webp,image/apng,image/png,image/jpeg,image/*,*/*;q=0.8")
                    .header("Accept-Language", "vi-VN,vi;q=0.9,en;q=0.7")
                    .header("Referer", referer)
                    .GET()
                    .build();

                HttpResponse<InputStream> remote = HTTP_CLIENT.send(
                    remoteRequest, HttpResponse.BodyHandlers.ofInputStream());
                try (InputStream input = remote.body()) {
                    if (remote.statusCode() >= 300 && remote.statusCode() < 400) {
                        String location = remote.headers().firstValue("Location").orElse("");
                        if (location.isBlank() || redirect == 3) return null;
                        current = current.resolve(location);
                        continue;
                    }
                    if (remote.statusCode() < 200 || remote.statusCode() >= 300) return null;
                    byte[] bytes = input.readNBytes(MAX_REMOTE_BYTES + 1);
                    if (bytes.length < 32 || bytes.length > MAX_REMOTE_BYTES) return null;

                    String contentType = remote.headers().firstValue("Content-Type")
                        .orElse("").split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
                    if (!contentType.startsWith("image/")) contentType = detectContentType(bytes);
                    if (contentType == null) return null;
                    return new CachedImage(bytes, contentType, false, null, 0);
                }
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
            // The caller generates a branded placeholder when all official sources fail.
        }
        return null;
    }
    private boolean isPublicHttpUri(URI uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (host == null || !("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))) {
            return false;
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                        || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                        || address.isMulticastAddress()) {
                    return false;
                }
            }
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private String detectContentType(byte[] bytes) {
        if (matchesAt(bytes, 0, 0x89, 0x50, 0x4e, 0x47)) return "image/png";
        if (matchesAt(bytes, 0, 0xff, 0xd8, 0xff)) return "image/jpeg";
        if (matchesAt(bytes, 0, 0x47, 0x49, 0x46, 0x38)) return "image/gif";
        if (matchesAt(bytes, 0, 0x52, 0x49, 0x46, 0x46)
                && matchesAt(bytes, 8, 0x57, 0x45, 0x42, 0x50)) {
            return "image/webp";
        }
        return null;
    }

    private boolean matchesAt(byte[] bytes, int offset, int... expected) {
        if (bytes.length < offset + expected.length) return false;
        for (int i = 0; i < expected.length; i++) {
            if ((bytes[offset + i] & 0xff) != expected[i]) return false;
        }
        return true;
    }

    private byte[] createPlaceholder(Product product) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, WIDTH, HEIGHT);

            graphics.setColor(new Color(248, 250, 252));
            graphics.fill(new RoundRectangle2D.Double(32, 32, WIDTH - 64, HEIGHT - 64, 24, 24));
            graphics.setColor(new Color(226, 232, 240));
            graphics.setStroke(new BasicStroke(3f));
            graphics.draw(new RoundRectangle2D.Double(33, 33, WIDTH - 66, HEIGHT - 66, 24, 24));

            drawImageIcon(graphics);
            drawCentered(graphics, safe(product.getBrand()).toUpperCase(), 480,
                    new Font("SansSerif", Font.BOLD, 24), new Color(225, 29, 72));
            drawCentered(graphics, fit(product.getProductName(), 58), 530,
                    new Font("SansSerif", Font.BOLD, 34), new Color(15, 23, 42));
            drawCentered(graphics, "Không tải được ảnh sản phẩm thật", 585,
                    new Font("SansSerif", Font.PLAIN, 23), new Color(71, 85, 105));
            drawCentered(graphics, "Hãy kiểm tra kết nối Internet hoặc mở nguồn chính hãng.", 625,
                    new Font("SansSerif", Font.PLAIN, 19), new Color(100, 116, 139));
        } finally {
            graphics.dispose();
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return output.toByteArray();
        } catch (IOException error) {
            throw new IllegalStateException("Không thể tạo ảnh dự phòng", error);
        }
    }

    private void drawImageIcon(Graphics2D graphics) {
        int x = 325;
        int y = 150;
        int width = 250;
        int height = 240;
        graphics.setColor(Color.WHITE);
        graphics.fill(new RoundRectangle2D.Double(x, y, width, height, 20, 20));
        graphics.setColor(new Color(148, 163, 184));
        graphics.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.draw(new RoundRectangle2D.Double(x, y, width, height, 20, 20));
        graphics.drawOval(x + 162, y + 42, 42, 42);
        graphics.drawLine(x + 36, y + 318 - 150, x + 96, y + 250 - 150);
        graphics.drawLine(x + 96, y + 250 - 150, x + 145, y + 304 - 150);
        graphics.drawLine(x + 145, y + 304 - 150, x + 185, y + 266 - 150);
        graphics.drawLine(x + 185, y + 266 - 150, x + 218, y + 318 - 150);
    }

    private void drawCentered(Graphics2D graphics, String text, int y, Font font, Color color) {
        graphics.setFont(font);
        graphics.setColor(color);
        FontMetrics metrics = graphics.getFontMetrics();
        graphics.drawString(text, (WIDTH - metrics.stringWidth(text)) / 2, y);
    }

    private String fit(String value, int maxLength) {
        String text = safe(value);
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "…";
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private record CachedImage(byte[] bytes, String contentType, boolean placeholder,
            String etag, long expiresAt) {}
}
