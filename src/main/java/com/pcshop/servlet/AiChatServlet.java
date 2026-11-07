package com.pcshop.servlet;

import com.pcshop.dao.ProductDAO;
import com.pcshop.model.Product;
import com.pcshop.util.AppConfig;
import com.pcshop.util.PresetBuildAdvisor;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/ai-chat")
public class AiChatServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_LOCAL_URL = "http://127.0.0.1:1234/api/v1/chat";
    private static final String HISTORY_KEY = AiChatServlet.class.getName() + ".history";
    private static final String COOLDOWN_KEY = AiChatServlet.class.getName() + ".localCooldown";
    private static final int MAX_MESSAGE_LENGTH = 1_200;
    private static final int MAX_BUILD_LENGTH = 8_000;
    private static final int MAX_HISTORY_TURNS = 4;
    private static final int MAX_ANSWER_LENGTH = 6_000;
    private static final int MAX_REQUESTS_PER_MINUTE = 20;
    private static final Pattern BUDGET_PATTERN = Pattern.compile(
            "(?i)(\\d+(?:[\\.,]\\d+)?)\\s*(tr|tri(?:e|\\u1ec7)u|m|million|c(?:u|\\u1ee7))");
    private static final Pattern BUILD_PART_LINE_PATTERN = Pattern.compile(
            "(?im)^(?:[-\\u2022]\\s*)?(CPU|Mainboard|RAM|GPU|SSD|PSU|Cooler|Case|Tản nhiệt|Vỏ máy)\\s*:\\s*.*?([0-9][0-9.,]*)\\s*(?:VND|đ)\\s*\\)?\\s*$");
    private static final Pattern TOTAL_LINE_PATTERN = Pattern.compile(
            "(?im)^(\\s*Tổng(?: cộng| giá| dự kiến| chính xác)?(?:\\s*\\([^)]*\\))?\\s*:\\s*)[0-9][0-9.,]*(?:\\s*(?:VND|đ))?.*$");
    private static final Pattern BUDGET_PERCENT_PATTERN = Pattern.compile(
            "(?i)\\d+(?:[\\.,]\\d+)?%\\s*(?:của\\s+)?ngân sách");
    private static final Map<String, RateWindow> RATE_LIMITS = new ConcurrentHashMap<>();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("X-Content-Type-Options", "nosniff");

        String message = clean(req.getParameter("message"));
        String build = limit(clean(req.getParameter("build")), MAX_BUILD_LENGTH);
        String preset = clean(req.getParameter("preset"));
        HttpSession session = req.getSession(true);
        if (!allowRequest(req)) {
            resp.setStatus(429);
            resp.setHeader("Retry-After", "60");
            writeAnswer(resp, "Bạn đang gửi yêu cầu quá nhanh. Vui lòng chờ một phút rồi thử lại nhé.",
                    "local", "rate_limited");
            return;
        }

        if (!preset.isBlank()) {
            String lastKeyAttribute = AiChatServlet.class.getName() + ".preset." + preset;
            String lastKey = clean((String) session.getAttribute(lastKeyAttribute));
            PresetBuildAdvisor.Advice advice = PresetBuildAdvisor.randomAdvice(
                    preset, productDAO.getActive(), lastKey);
            if (!advice.text().isBlank()) {
                session.setAttribute(lastKeyAttribute, advice.key());
                writeAnswer(resp, advice.text(), "preset", "ready_preset");
                return;
            }
        }

        if (message.isBlank()) {
            writeAnswer(resp, "Bạn hãy nhập ngân sách và mục đích sử dụng để mình tư vấn chính xác hơn nhé.",
                    "local", "empty_message");
            return;
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            writeAnswer(resp, "Nội dung hơi dài. Bạn hãy rút gọn còn ngân sách, nhu cầu chính và màn hình đang dùng nhé.",
                    "local", "message_too_long");
            return;
        }

        if (!localAiEnabled()) {
            String answer = friendlyFallback(message, build);
            rememberTurn(session, message, answer);
            writeAnswer(resp, answer, "local-fallback", "local_ai_disabled");
            return;
        }

        long cooldownUntil = sessionLong(session, COOLDOWN_KEY);
        if (System.currentTimeMillis() < cooldownUntil) {
            String answer = friendlyFallback(message, build);
            rememberTurn(session, message, answer);
            writeAnswer(resp, answer, "local-fallback", "local_ai_cooldown");
            return;
        }

        try {
            URI endpoint = localEndpoint();
            String model = resolveLocalModel(endpoint);
            if (model.isBlank()) throw new IOException("No local model is loaded.");

            String input = buildAiInput(session, message, build);
            String payload = "{"
                    + "\"model\":\"" + escapeJson(model) + "\","
                    + "\"system_prompt\":\"" + escapeJson(systemPrompt()) + "\","
                    + "\"input\":\"" + escapeJson(input) + "\","
                    + "\"reasoning\":\"off\","
                    + "\"temperature\":0.15,"
                    + "\"max_output_tokens\":800,"
                    + "\"store\":false"
                    + "}";

            HttpRequest.Builder request = HttpRequest.newBuilder()
                    .uri(endpoint)
                    .timeout(Duration.ofSeconds(90))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
            String token = AppConfig.get("LOCAL_AI_TOKEN", "local.ai.token");
            if (!token.isBlank()) request.header("Authorization", "Bearer " + token);
            HttpResponse<String> response = HTTP.send(request.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                session.removeAttribute(COOLDOWN_KEY);
                BigDecimal budget = extractBudget(message);
                String answer = limit(clean(extractAnswer(response.body())), MAX_ANSWER_LENGTH);
                answer = correctBuildTotals(answer, budget);
                BigDecimal selectedTotal = extractBuildTotal(answer);
                if (budget.signum() > 0 && (selectedTotal.signum() <= 0 || selectedTotal.compareTo(budget) > 0)) {
                    PresetBuildAdvisor.Advice controlled = PresetBuildAdvisor.budgetAdvice(
                            message, budget, productDAO.getActive());
                    if (!controlled.text().isBlank()) answer = controlled.text();
                }
                if (!answer.isBlank()) {
                    rememberTurn(session, message, answer);
                    writeAnswer(resp, answer, "local-ai", "ready_local_ai");
                    return;
                }
                getServletContext().log("Local AI returned HTTP 2xx without a message content item.");
            } else {
                getServletContext().log("Local AI request returned HTTP " + response.statusCode() + ".");
                session.setAttribute(COOLDOWN_KEY, System.currentTimeMillis() + 15_000L);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            getServletContext().log("Local AI request was interrupted.");
        } catch (Exception e) {
            getServletContext().log("Local AI request failed: " + e.getClass().getSimpleName());
        }

        String answer = friendlyFallback(message, build);
        rememberTurn(session, message, answer);
        writeAnswer(resp, answer, "local-fallback", "local_ai_unavailable");
    }

    private boolean localAiEnabled() {
        String value = AppConfig.get("LOCAL_AI_ENABLED", "local.ai.enabled", "true");
        return !"false".equalsIgnoreCase(value) && !"0".equals(value);
    }

    private URI localEndpoint() throws IOException {
        URI endpoint;
        try {
            endpoint = URI.create(AppConfig.get("LOCAL_AI_URL", "local.ai.url", DEFAULT_LOCAL_URL));
        } catch (IllegalArgumentException error) {
            throw new IOException("Invalid local AI URL.", error);
        }
        String host = clean(endpoint.getHost()).toLowerCase(Locale.ROOT);
        boolean loopback = "127.0.0.1".equals(host) || "localhost".equals(host) || "::1".equals(host);
        if (!loopback || !"http".equalsIgnoreCase(endpoint.getScheme())) {
            throw new IOException("Local AI endpoint must use HTTP on this computer.");
        }
        return endpoint;
    }

    private String resolveLocalModel(URI endpoint) throws IOException, InterruptedException {
        String configured = AppConfig.get("LOCAL_AI_MODEL", "local.ai.model");
        if (!configured.isBlank()) return configured;

        URI modelsEndpoint = endpoint.resolve("/v1/models");
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(modelsEndpoint)
                .timeout(Duration.ofSeconds(5))
                .GET();
        String token = AppConfig.get("LOCAL_AI_TOKEN", "local.ai.token");
        if (!token.isBlank()) request.header("Authorization", "Bearer " + token);
        HttpResponse<String> response = HTTP.send(request.build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) return "";
        int idKey = response.body().indexOf("\"id\"");
        JsonValue id = idKey < 0 ? null : readJsonStringValue(response.body(), idKey);
        return id == null ? "" : clean(id.value());
    }

    private boolean allowRequest(HttpServletRequest request) {
        long minute = System.currentTimeMillis() / 60_000L;
        String key = request.getRemoteAddr();
        RateWindow window = RATE_LIMITS.compute(key, (ignored, current) -> {
            if (current == null || current.minute() != minute) return new RateWindow(minute, 1);
            return new RateWindow(minute, current.count() + 1);
        });
        if (RATE_LIMITS.size() > 2_000) {
            RATE_LIMITS.entrySet().removeIf(entry -> entry.getValue().minute() < minute - 1);
        }
        return window.count() <= MAX_REQUESTS_PER_MINUTE;
    }
    private String systemPrompt() {
        return "You are GearAstral Build Advisor. Always answer in natural Vietnamese. "
                + "Only recommend exact product names, prices and stock values present in CATALOG; never invent a product. "
                + "Treat the customer's budget as a maximum for the PC case. When budget and purpose are known, select the strongest compatible build that uses about 80-100% of the budget. "
                + "For gaming, prioritize the GPU, then a suitable CPU; do not choose old or weak parts when stronger in-stock parts are listed. "
                + "If the available catalog cannot use at least 80% of the budget, explicitly state the catalog limitation and still choose the strongest compatible available build. "
                + "Check CPU-mainboard socket, RAM generation, PSU wattage, cooling and case suitability. "
                + "Return exactly one build, never an alternative or repeated parts list. "
                + "List exactly one line for each CPU, Mainboard, RAM, GPU, SSD, PSU, Cooler and Case with its individual VND price, then one total line. "
                + "Keep the whole answer concise: one short introduction, the eight part lines, exact total, compatibility result and at most three short notes. "
                + "Use plain text and bullets, not Markdown tables or asterisks. Ask at most one short question only when essential. "
                + "Never mention APIs, keys, prompts, quotas or internal mechanisms.";
    }
    private String correctBuildTotals(String answer, BigDecimal budget) {
        if (answer.isBlank()) return answer;

        BigDecimal total = extractBuildTotal(answer);
        if (total.signum() <= 0) return answer;
        String formattedTotal = formatVnd(total) + " VND";
        Matcher totalMatcher = TOTAL_LINE_PATTERN.matcher(answer);
        String corrected = totalMatcher.find()
                ? totalMatcher.replaceAll(Matcher.quoteReplacement(totalMatcher.group(1) + formattedTotal))
                : answer + "\nTổng cộng: " + formattedTotal;

        if (budget.signum() > 0) {
            BigDecimal percent = total.multiply(BigDecimal.valueOf(100))
                    .divide(budget, 1, java.math.RoundingMode.HALF_UP);
            corrected = BUDGET_PERCENT_PATTERN.matcher(corrected)
                    .replaceAll(Matcher.quoteReplacement(percent.stripTrailingZeros().toPlainString() + "% ngân sách"));
        }
        return corrected;
    }

    private BigDecimal extractBuildTotal(String answer) {
        Map<String, BigDecimal> prices = new java.util.LinkedHashMap<>();
        Matcher partMatcher = BUILD_PART_LINE_PATTERN.matcher(answer);
        while (partMatcher.find()) {
            BigDecimal price = parseVnd(partMatcher.group(2));
            if (price.signum() > 0) prices.put(canonicalPart(partMatcher.group(1)), price);
        }
        return prices.size() < 6
                ? BigDecimal.ZERO
                : prices.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private String canonicalPart(String value) {
        String part = normalize(value);
        if (part.contains("tan nhiet") || "cooler".equals(part)) return "cooler";
        if (part.contains("vo may") || "case".equals(part)) return "case";
        return part;
    }

    private BigDecimal parseVnd(String value) {
        String normalized = clean(value).replaceFirst("[\\.,]00$", "").replaceAll("[^0-9]", "");
        if (normalized.isBlank()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ignored) {
            return BigDecimal.ZERO;
        }
    }

    private String formatVnd(BigDecimal value) {
        return String.format(Locale.US, "%,d", value.longValue()).replace(',', '.');
    }
    private String buildAiInput(HttpSession session, String message, String build) {
        StringBuilder input = new StringBuilder();
        BigDecimal budget = extractBudget(message);
        String history = conversationHistory(session);
        if (budget.signum() <= 0 && !history.isBlank()) {
            input.append("RECENT CONVERSATION:\n").append(history).append("\n");
        }
        input.append("CURRENT CUSTOMER REQUEST:\n").append(message).append("\n\n");
        if (budget.signum() > 0) {
            input.append("DETECTED MAXIMUM BUDGET: ").append(budget.toPlainString()).append(" VND.\n")
                    .append("Build close to 80-100% of this amount using the strongest compatible in-stock catalog products. ")
                    .append("Ignore earlier recommendations made for another budget.\n\n");
        }
        if (!build.isBlank() && !"[]".equals(build)) {
            input.append("PARTS ALREADY SELECTED BY CUSTOMER:\n").append(build).append("\n\n");
        }
        input.append("CURRENT IN-STOCK CATALOG, RANKED FOR THIS BUDGET:\n")
                .append(buildCatalogContext(message));
        return input.toString();
    }
    private String buildCatalogContext(String message) {
        List<Product> products = new ArrayList<>(productDAO.getActive());
        if (products.isEmpty()) return "Catalog hiện chưa có sản phẩm khả dụng.";

        String query = normalize(message);
        BigDecimal budget = extractBudget(message);
        Comparator<Product> ranking = Comparator
                .comparingInt((Product p) -> relevance(p, query)).reversed()
                .thenComparingInt(Product::getCatId);
        if (budget.signum() > 0) {
            ranking = ranking
                    .thenComparing(product -> budgetDistance(product, budget))
                    .thenComparing(Product::getPrice, Comparator.nullsLast(Comparator.reverseOrder()));
        } else {
            ranking = ranking.thenComparing(Product::getPrice, Comparator.nullsLast(Comparator.naturalOrder()));
        }
        products.sort(ranking);

        int[] perCategory = new int[32];
        StringBuilder out = new StringBuilder();
        int total = 0;
        for (Product product : products) {
            int category = Math.max(0, Math.min(product.getCatId(), perCategory.length - 1));
            int categoryLimit = category == 9 || category == 10 ? 4 : 7;
            if (perCategory[category] >= categoryLimit || product.getStockQty() <= 0) continue;

            out.append("- [").append(oneLine(product.getCatName(), 45)).append("] ")
                    .append(oneLine(product.getProductName(), 95))
                    .append(" | model: ").append(oneLine(product.getModel(), 55))
                    .append(" | giá: ").append(product.getPrice() == null ? "liên hệ" : product.getPrice().toPlainString() + " VND")
                    .append(" | tồn: ").append(product.getStockQty())
                    .append(" | ").append(oneLine(product.getSpecs(), 150))
                    .append('\n');
            perCategory[category]++;
            total++;
            if (total >= 62) break;
        }
        return out.toString();
    }

    private int relevance(Product product, String query) {
        if (query.isBlank()) return 0;
        String haystack = normalize(product.getProductName() + " " + product.getBrand() + " "
                + product.getModel() + " " + product.getCatName() + " " + product.getSpecs());
        int score = 0;
        for (String token : query.split("\\s+")) {
            if (token.length() >= 3 && haystack.contains(token)) score += token.length();
        }
        if ((query.contains("game") || query.contains("gaming") || query.contains("3a") || query.contains("aaa"))
                && (product.getCatId() == 1 || product.getCatId() == 4)) score += 8;
        if ((query.contains("do hoa") || query.contains("render") || query.contains(" ai "))
                && (product.getCatId() == 1 || product.getCatId() == 3 || product.getCatId() == 4)) score += 8;
        return score;
    }

    private BigDecimal extractBudget(String message) {
        Matcher matcher = BUDGET_PATTERN.matcher(clean(message));
        if (!matcher.find()) return BigDecimal.ZERO;
        try {
            BigDecimal millions = new BigDecimal(matcher.group(1).replace(',', '.'));
            return millions.multiply(BigDecimal.valueOf(1_000_000L));
        } catch (NumberFormatException ignored) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal budgetDistance(Product product, BigDecimal budget) {
        if (product.getPrice() == null) return BigDecimal.valueOf(Long.MAX_VALUE);
        double share = switch (product.getCatId()) {
            case 1 -> 0.16;
            case 2 -> 0.10;
            case 3 -> 0.07;
            case 4 -> 0.50;
            case 5 -> 0.06;
            case 6 -> 0.05;
            case 7 -> 0.035;
            case 8 -> 0.035;
            default -> 0.05;
        };
        BigDecimal target = budget.multiply(BigDecimal.valueOf(share));
        return product.getPrice().subtract(target).abs();
    }
    private String friendlyFallback(String message, String build) {
        String q = normalize(message);
        String selected = build.isBlank() || "[]".equals(build)
                ? ""
                : "\n\nMình đã ghi nhận các linh kiện bạn đang chọn và sẽ ưu tiên kiểm tra độ tương thích của chúng.";
        if (q.contains("ai") || q.contains("machine learning") || q.contains("5090")) {
            return "Với máy AI, bạn nên ưu tiên GPU có VRAM lớn, RAM 64-96GB, SSD NVMe 2TB và nguồn từ 1000W chuẩn Gold. "
                    + "CPU nhiều nhân và case thoáng sẽ giúp chạy tác vụ dài ổn định hơn. Bạn cho mình ngân sách tối đa để chốt đúng linh kiện đang có hàng nhé."
                    + selected;
        }
        if (q.contains("render") || q.contains("do hoa") || q.contains("premiere") || q.contains("blender")) {
            return "Cấu hình đồ họa nên cân bằng CPU nhiều nhân, GPU NVIDIA hỗ trợ CUDA, RAM tối thiểu 32GB và SSD 1-2TB. "
                    + "Nếu render 4K thường xuyên, nên nâng RAM lên 64GB, dùng tản tốt và nguồn 850W Gold. Bạn cho mình ngân sách để lên danh sách cụ thể nhé."
                    + selected;
        }
        if (q.contains("game") || q.contains("gaming") || q.contains("fps")) {
            return "Với PC gaming, nên dành phần lớn ngân sách cho GPU, sau đó chọn CPU và màn hình phù hợp độ phân giải. "
                    + "Mức 1440p nên có RAM 32GB, SSD NVMe 1-2TB và nguồn 750-850W Gold. Bạn cho mình ngân sách và game thường chơi để chốt cấu hình sát nhất nhé."
                    + selected;
        }
        if (q.contains("van phong") || q.contains("office") || q.contains("hoc tap")) {
            return "Máy văn phòng nên ưu tiên độ ổn định và khả năng nâng cấp: CPU 6-8 nhân, RAM 16-32GB, SSD 1TB và nguồn chất lượng tốt. "
                    + "Nếu không làm đồ họa hoặc chơi game, có thể dùng đồ họa tích hợp để tiết kiệm ngân sách."
                    + selected;
        }
        return "Mình có thể tư vấn theo catalog của GearAstral. Bạn cho mình 3 thông tin: ngân sách tối đa, nhu cầu chính và độ phân giải màn hình đang dùng. "
                + "Mình sẽ đề xuất cấu hình, tổng giá và kiểm tra tương thích từng linh kiện." + selected;
    }

    @SuppressWarnings("unchecked")
    private String conversationHistory(HttpSession session) {
        Object value = session.getAttribute(HISTORY_KEY);
        if (!(value instanceof Deque<?>)) return "";
        StringBuilder out = new StringBuilder();
        for (ChatTurn turn : (Deque<ChatTurn>) value) {
            out.append("Khách: ").append(turn.question()).append('\n')
                    .append("Tư vấn: ").append(turn.answer()).append('\n');
        }
        return limit(out.toString(), 7_000);
    }

    @SuppressWarnings("unchecked")
    private void rememberTurn(HttpSession session, String question, String answer) {
        Deque<ChatTurn> history;
        Object value = session.getAttribute(HISTORY_KEY);
        if (value instanceof Deque<?>) history = (Deque<ChatTurn>) value;
        else {
            history = new ArrayDeque<>();
            session.setAttribute(HISTORY_KEY, history);
        }
        history.addLast(new ChatTurn(limit(question, 700), limit(answer, 2_500)));
        while (history.size() > MAX_HISTORY_TURNS) history.removeFirst();
    }

    private String extractAnswer(String body) {
        if (body == null || body.isBlank()) return "";
        StringBuilder answer = new StringBuilder();
        int cursor = 0;
        while ((cursor = body.indexOf("\"type\"", cursor)) >= 0) {
            JsonValue type = readJsonStringValue(body, cursor);
            if (type == null) break;
            cursor = type.end();
            if (!"output_text".equals(type.value())) continue;

            int textKey = body.indexOf("\"text\"", cursor);
            if (textKey < 0) break;
            JsonValue text = readJsonStringValue(body, textKey);
            if (text == null) continue;
            if (answer.length() > 0) answer.append('\n');
            answer.append(text.value());
            cursor = text.end();
        }

        if (answer.length() == 0) {
            int convenienceField = body.indexOf("\"output_text\"");
            JsonValue value = convenienceField < 0 ? null : readJsonStringValue(body, convenienceField);
            if (value != null) answer.append(value.value());
        }

        if (answer.length() == 0) {
            cursor = 0;
            while ((cursor = body.indexOf("\"type\"", cursor)) >= 0) {
                JsonValue type = readJsonStringValue(body, cursor);
                if (type == null) break;
                cursor = type.end();
                if (!"message".equals(type.value())) continue;

                int contentKey = body.indexOf("\"content\"", cursor);
                if (contentKey < 0) break;
                JsonValue content = readJsonStringValue(body, contentKey);
                if (content == null) continue;
                if (answer.length() > 0) answer.append('\n');
                answer.append(content.value());
                cursor = content.end();
            }
        }
        return answer.toString();
    }

    private JsonValue readJsonStringValue(String json, int keyPosition) {
        int colon = json.indexOf(':', keyPosition);
        if (colon < 0) return null;
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start >= json.length() || json.charAt(start) != '"') return null;
        start++;
        int end = findStringEnd(json, start);
        if (end < 0) return null;
        return new JsonValue(unescapeJson(json.substring(start, end)), end + 1);
    }

    private int findStringEnd(String value, int start) {
        boolean escaped = false;
        for (int i = start; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' && !escaped) return i;
            if (c == '\\') escaped = !escaped;
            else escaped = false;
        }
        return -1;
    }

    private void writeAnswer(HttpServletResponse resp, String answer, String mode, String status) throws IOException {
        resp.getWriter().write("{\"answer\":\"" + escapeJson(answer)
                + "\",\"mode\":\"" + escapeJson(mode)
                + "\",\"status\":\"" + escapeJson(status) + "\"}");
    }

    private long sessionLong(HttpSession session, String key) {
        Object value = session.getAttribute(key);
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String limit(String value, int maxLength) {
        if (value == null) return "";
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String oneLine(String value, int maxLength) {
        String cleanValue = clean(value).replace('\n', ' ').replace('\r', ' ');
        return limit(cleanValue, maxLength);
    }

    private String normalize(String value) {
        return clean(value).toLowerCase(Locale.ROOT)
                .replace('đ', 'd')
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y");
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        StringBuilder out = new StringBuilder(value.length() + 32);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        return out.toString();
    }

    private String unescapeJson(String value) {
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\\' || i + 1 >= value.length()) {
                out.append(c);
                continue;
            }
            char next = value.charAt(++i);
            switch (next) {
                case 'n' -> out.append('\n');
                case 'r' -> out.append('\r');
                case 't' -> out.append('\t');
                case 'b' -> out.append('\b');
                case 'f' -> out.append('\f');
                case '"' -> out.append('"');
                case '\\' -> out.append('\\');
                case '/' -> out.append('/');
                case 'u' -> {
                    if (i + 4 < value.length()) {
                        String hex = value.substring(i + 1, i + 5);
                        try {
                            out.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        } catch (NumberFormatException e) {
                            out.append("\\u").append(hex);
                            i += 4;
                        }
                    } else out.append("\\u");
                }
                default -> out.append(next);
            }
        }
        return out.toString();
    }

    private record RateWindow(long minute, int count) {}

    private record ChatTurn(String question, String answer) {
    }

    private record JsonValue(String value, int end) {
    }
}
