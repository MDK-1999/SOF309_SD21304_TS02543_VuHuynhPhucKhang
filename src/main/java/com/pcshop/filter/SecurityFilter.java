package com.pcshop.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

public class SecurityFilter implements Filter {
    public static final String CSRF_SESSION_KEY = SecurityFilter.class.getName() + ".token";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = requestPath(req);
        req.setAttribute("canonicalUrl", canonicalUrl(req, path));

        addSecurityHeaders(req, resp);
        if (isStaticAsset(path)) {
            resp.setHeader("Cache-Control", "public, max-age=604800, immutable");
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(true);
        String csrfToken = (String) session.getAttribute(CSRF_SESSION_KEY);
        if (csrfToken == null) {
            csrfToken = newToken();
            session.setAttribute(CSRF_SESSION_KEY, csrfToken);
        }
        req.setAttribute("csrfToken", csrfToken);

        if (!SAFE_METHODS.contains(req.getMethod()) && !validCsrf(req, csrfToken)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().write("Yêu cầu không hợp lệ hoặc phiên đã hết hạn. Vui lòng tải lại trang.");
            return;
        }

        if (isPrivatePath(path)) {
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            resp.setHeader("Pragma", "no-cache");
        }
        chain.doFilter(request, response);
    }

    private void addSecurityHeaders(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        resp.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=(), payment=()");
        resp.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        resp.setHeader("Cross-Origin-Resource-Policy", "same-origin");
        resp.setHeader("Content-Security-Policy",
                "default-src 'self'; base-uri 'self'; object-src 'none'; frame-ancestors 'none'; "
                + "form-action 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; "
                + "script-src 'self' 'unsafe-inline'; connect-src 'self'; "
                + "frame-src https://maps.google.com https://www.google.com");
        if (req.isSecure()) {
            resp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
    }

    private boolean validCsrf(HttpServletRequest req, String expected) {
        String supplied = req.getHeader("X-CSRF-Token");
        if (supplied == null || supplied.isBlank()) supplied = req.getParameter("_csrf");
        if (supplied == null) return false;
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                supplied.getBytes(StandardCharsets.UTF_8));
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String requestPath(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String context = req.getContextPath();
        return uri.substring(Math.min(context.length(), uri.length()));
    }

    private String canonicalUrl(HttpServletRequest req, String path) {
        int port = req.getServerPort();
        boolean defaultPort = (req.isSecure() && port == 443) || (!req.isSecure() && port == 80);
        String authority = req.getServerName() + (defaultPort ? "" : ":" + port);
        return req.getScheme() + "://" + authority + req.getContextPath() + path;
    }
    private boolean isStaticAsset(String path) {
        return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/");
    }

    private boolean isPrivatePath(String path) {
        return path.startsWith("/admin/") || path.startsWith("/staff/")
                || path.startsWith("/customer/") || path.equals("/dashboard")
                || path.equals("/login") || path.equals("/register");
    }
}
