package com.pcshop.util;

import com.pcshop.model.Product;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PresetBuildAdvisor {
    private static final Locale VIETNAM = new Locale("vi", "VN");
    private static final Pattern WATT_PATTERN = Pattern.compile("(?<!\\d)(\\d{3,4})\\s*w", Pattern.CASE_INSENSITIVE);
    private static final Pattern RYZEN_MODEL_PATTERN = Pattern.compile("ryzen\\s+\\d\\s+(\\d{4})", Pattern.CASE_INSENSITIVE);
    private static final Map<String, Profile> PROFILES = createProfiles();
    private static final int OPTION_LIMIT = 7;
    private static final int POOL_LIMIT = 4;

    private PresetBuildAdvisor() {
    }

    public static String randomAdvice(String presetId, List<Product> catalog) {
        return randomAdvice(presetId, catalog, "").text();
    }

    public static Advice randomAdvice(String presetId, List<Product> catalog, String excludedKey) {
        Profile profile = PROFILES.get(presetId);
        if (profile == null || catalog == null || catalog.isEmpty()) return Advice.empty();

        Map<Integer, List<Product>> categories = groupAvailableProducts(catalog);
        List<Candidate> candidates = generateCandidates(profile, categories);
        List<Candidate> options = selectSeven(candidates);
        if (options.isEmpty()) {
            return new Advice("", "Hiện chưa đủ linh kiện còn hàng để tạo cấu hình " + profile.priceLabel()
                    + ". Vui lòng kiểm tra lại tồn kho hoặc chọn mức giá khác.", 0);
        }

        List<Candidate> selectable = options.stream()
                .filter(option -> !option.key().equals(excludedKey))
                .toList();
        if (selectable.isEmpty()) selectable = options;
        Candidate selected = selectable.get(ThreadLocalRandom.current().nextInt(selectable.size()));
        int optionNumber = options.indexOf(selected) + 1;
        return new Advice(selected.key(), formatAnswer(profile, selected, optionNumber, options.size()), options.size());
    }

    public static Advice budgetAdvice(String message, BigDecimal budget, List<Product> catalog) {
        if (budget == null || budget.signum() <= 0 || catalog == null || catalog.isEmpty()) return Advice.empty();

        Profile profile = profileForRequest(message, budget);
        List<Candidate> candidates = generateCandidates(profile, groupAvailableProducts(catalog)).stream()
                .filter(candidate -> candidate.total().compareTo(budget) <= 0)
                .toList();
        List<Candidate> options = selectSeven(candidates);
        if (options.isEmpty()) return Advice.empty();

        Candidate selected = options.get(0);
        return new Advice(selected.key(), formatBudgetAnswer(profile, selected), options.size());
    }

    private static Profile profileForRequest(String message, BigDecimal budget) {
        String query = java.text.Normalizer.normalize(safe(message), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ', 'd').toLowerCase(Locale.ROOT);
        UseCase type;
        if (containsAny(query, "machine learning", "local ai", "llm", "cuda", "tri tue nhan tao")) type = UseCase.AI;
        else if (containsAny(query, "do hoa", "render", "premiere", "blender", "after effects", "cad")) type = UseCase.CREATOR;
        else if (containsAny(query, "4k", "2160p")) type = UseCase.GAMING_4K;
        else if (containsAny(query, "2k", "1440p", "qhd")) type = UseCase.GAMING_QHD;
        else if (containsAny(query, "game", "gaming", "aaa", "fps", "esport")) type = UseCase.GAMING_FHD;
        else type = UseCase.OFFICE;

        int minRam = type == UseCase.OFFICE ? (budget.compareTo(BigDecimal.valueOf(25_000_000L)) >= 0 ? 32 : 16)
                : (budget.compareTo(BigDecimal.valueOf(55_000_000L)) >= 0 ? 64 : 32);
        String purpose = switch (type) {
            case OFFICE -> "Văn phòng, học tập, lập trình và đa nhiệm.";
            case GAMING_FHD -> "Gaming Full HD và esport FPS cao.";
            case GAMING_QHD -> "Gaming 2K/1440p và game AAA.";
            case CREATOR -> "Đồ họa, dựng phim và render.";
            case GAMING_4K -> "Gaming 4K và ray tracing.";
            case WORKSTATION -> "Workstation chuyên nghiệp.";
            case AI -> "AI local, machine learning và CUDA.";
        };
        String note = type == UseCase.OFFICE
                ? "Ưu tiên độ ổn định, RAM và SSD; có thể nâng GPU sau."
                : "Đã ưu tiên GPU phù hợp nhu cầu nhưng không vượt ngân sách tối đa.";
        return new Profile(type, budget, "tối đa " + formatPrice(budget), minRam,
                type != UseCase.OFFICE, purpose, note);
    }
    private static Map<Integer, List<Product>> groupAvailableProducts(List<Product> catalog) {
        Map<Integer, List<Product>> categories = new HashMap<>();
        for (Product product : catalog) {
            if (product == null || !product.isActive() || product.getStockQty() <= 0
                    || product.getPrice() == null || product.getPrice().signum() <= 0) continue;
            categories.computeIfAbsent(product.getCatId(), ignored -> new ArrayList<>()).add(product);
        }
        return categories;
    }

    private static List<Candidate> generateCandidates(Profile profile, Map<Integer, List<Product>> categories) {
        List<Product> cpus = nearest(categories.get(1), desired(profile, 1), product -> acceptCpu(profile, product));
        List<Product> boards = nearest(categories.get(2), desired(profile, 2), product -> true);
        List<Product> rams = nearest(categories.get(3), desired(profile, 3), product -> ramCapacity(product) >= profile.minRamGb());
        List<Product> gpus = profile.gpuRequired()
                ? nearest(categories.get(4), desired(profile, 4), product -> acceptGpu(profile, product))
                : Collections.singletonList(null);
        List<Product> ssds = nearest(categories.get(5), desired(profile, 5), product -> true);
        List<Product> psus = nearest(categories.get(6), desired(profile, 6), product -> true);
        List<Product> coolers = nearest(categories.get(7), desired(profile, 7), product -> true);
        List<Product> cases = nearest(categories.get(8), desired(profile, 8), product -> true);

        if (cpus.isEmpty() || boards.isEmpty() || rams.isEmpty() || gpus.isEmpty() || ssds.isEmpty()
                || psus.isEmpty() || coolers.isEmpty() || cases.isEmpty()) return List.of();

        BigDecimal minimum = profile.target().multiply(BigDecimal.valueOf(0.78));
        BigDecimal maximum = profile.target().multiply(BigDecimal.valueOf(1.22));
        List<Candidate> candidates = new ArrayList<>();

        for (Product cpu : cpus) {
            for (Product board : boards) {
                if (!cpuBoardCompatible(cpu, board)) continue;
                for (Product ram : rams) {
                    if (!ramBoardCompatible(ram, board)) continue;
                    for (Product gpu : gpus) {
                        for (Product ssd : ssds) {
                            for (Product psu : psus) {
                                if (!powerCompatible(gpu, psu)) continue;
                                for (Product cooler : coolers) {
                                    for (Product pcCase : cases) {
                                        List<Product> parts = parts(cpu, board, ram, gpu, ssd, psu, cooler, pcCase);
                                        BigDecimal total = total(parts);
                                        if (total.compareTo(minimum) < 0 || total.compareTo(maximum) > 0) continue;
                                        String key = parts.stream().map(Product::getProductName).reduce((a, b) -> a + "|" + b).orElse("");
                                        String coreKey = cpu.getProductName() + "|"
                                                + (gpu == null ? "IGPU" : gpu.getProductName()) + "|" + board.getProductName();
                                        candidates.add(new Candidate(key, coreKey, parts, total,
                                                total.subtract(profile.target()).abs()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        candidates.sort(Comparator.comparing(Candidate::distance).thenComparing(Candidate::key));
        return candidates;
    }

    private static List<Candidate> selectSeven(List<Candidate> candidates) {
        if (candidates.isEmpty()) return List.of();
        List<Candidate> selected = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        Set<String> coreKeys = new HashSet<>();

        for (Candidate candidate : candidates) {
            if (coreKeys.add(candidate.coreKey()) && keys.add(candidate.key())) selected.add(candidate);
            if (selected.size() == OPTION_LIMIT) return selected;
        }
        for (Candidate candidate : candidates) {
            if (keys.add(candidate.key())) selected.add(candidate);
            if (selected.size() == OPTION_LIMIT) break;
        }
        return List.copyOf(selected);
    }

    private static List<Product> nearest(List<Product> products, BigDecimal desired, Predicate<Product> filter) {
        if (products == null) return List.of();
        return products.stream()
                .filter(filter)
                .sorted(Comparator.comparing(product -> product.getPrice().subtract(desired).abs()))
                .limit(POOL_LIMIT)
                .toList();
    }

    private static boolean acceptCpu(Profile profile, Product cpu) {
        String text = productText(cpu);
        if (profile.type() == UseCase.OFFICE) return hasIntegratedGraphics(text);
        if (profile.type() == UseCase.GAMING_4K) return cpu.getPrice().compareTo(BigDecimal.valueOf(8_000_000)) >= 0;
        if (profile.type() == UseCase.WORKSTATION || profile.type() == UseCase.AI) {
            return cpu.getPrice().compareTo(BigDecimal.valueOf(11_000_000)) >= 0;
        }
        return true;
    }

    private static boolean acceptGpu(Profile profile, Product gpu) {
        String text = productText(gpu);
        BigDecimal price = gpu.getPrice();
        return switch (profile.type()) {
            case OFFICE -> false;
            case GAMING_FHD -> price.compareTo(BigDecimal.valueOf(10_000_000)) >= 0
                    && price.compareTo(BigDecimal.valueOf(17_500_000)) <= 0;
            case GAMING_QHD -> price.compareTo(BigDecimal.valueOf(13_000_000)) >= 0
                    && price.compareTo(BigDecimal.valueOf(27_000_000)) <= 0;
            case CREATOR -> text.contains("nvidia") && price.compareTo(BigDecimal.valueOf(18_000_000)) >= 0;
            case GAMING_4K -> price.compareTo(BigDecimal.valueOf(25_000_000)) >= 0;
            case WORKSTATION -> text.contains("nvidia") && price.compareTo(BigDecimal.valueOf(25_000_000)) >= 0;
            case AI -> text.contains("5090");
        };
    }

    private static boolean cpuBoardCompatible(Product cpu, Product board) {
        String cpuText = productText(cpu);
        String boardText = productText(board);
        boolean amdCpu = cpuText.contains("amd") || cpuText.contains("ryzen");
        boolean intelCpu = cpuText.contains("intel") || cpuText.contains("core i") || cpuText.contains("core ultra");
        boolean amdBoard = containsAny(boardText, "am4", "am5", "b450", "b550", "b650", "b850", "x570", "x670", "x870", "a520", "a620");
        boolean intelBoard = containsAny(boardText, "lga", "h410", "h510", "h610", "h710", "b460", "b560", "b660", "b760", "b860", "z490", "z590", "z690", "z790", "z890");
        if (amdCpu && intelBoard) return false;
        if (intelCpu && amdBoard) return false;

        String cpuSocket = cpuSocket(cpuText);
        String boardSocket = boardSocket(boardText);
        return cpuSocket.isBlank() || boardSocket.isBlank() || cpuSocket.equals(boardSocket);
    }

    private static String cpuSocket(String text) {
        if (text.contains("am4")) return "am4";
        if (text.contains("am5")) return "am5";
        if (text.contains("lga1200")) return "lga1200";
        if (text.contains("lga1700")) return "lga1700";
        if (text.contains("lga1851")) return "lga1851";
        if (text.contains("ryzen")) {
            Matcher model = RYZEN_MODEL_PATTERN.matcher(text);
            if (model.find()) return Integer.parseInt(model.group(1)) >= 7000 ? "am5" : "am4";
        }
        if (text.contains("core ultra")) return "lga1851";
        Matcher intel = Pattern.compile("\\bi[3579][ -]?(\\d{4,5})\\w*\\b", Pattern.CASE_INSENSITIVE).matcher(text);
        if (intel.find()) {
            int model = Integer.parseInt(intel.group(1));
            int generation = model >= 10000 ? model / 1000 : model / 100;
            if (generation <= 11) return "lga1200";
            if (generation <= 14) return "lga1700";
        }
        return "";
    }

    private static String boardSocket(String text) {
        if (containsAny(text, "am4", "b450", "b550", "x570", "a520")) return "am4";
        if (containsAny(text, "am5", "b650", "b850", "x670", "x870", "a620")) return "am5";
        if (containsAny(text, "lga1200", "h410", "h510", "b460", "b560", "z490", "z590")) return "lga1200";
        if (containsAny(text, "lga1700", "h610", "h710", "b660", "b760", "z690", "z790")) return "lga1700";
        if (containsAny(text, "lga1851", "b860", "z890")) return "lga1851";
        return "";
    }
    private static boolean ramBoardCompatible(Product ram, Product board) {
        String ramText = productText(ram);
        String boardText = productText(board);
        if (boardText.contains("ddr4") && ramText.contains("ddr5")) return false;
        if (boardText.contains("ddr5") && ramText.contains("ddr4")) return false;
        return true;
    }

    private static boolean powerCompatible(Product gpu, Product psu) {
        if (gpu == null) return true;
        int availableWatts = wattage(psu);
        return availableWatts == 0 || availableWatts >= requiredWatts(gpu);
    }

    private static int requiredWatts(Product gpu) {
        String text = productText(gpu);
        if (text.contains("5090")) return 1000;
        if (containsAny(text, "5080", "4080", "7900 xtx")) return 850;
        if (containsAny(text, "4070 ti", "5070 ti", "9070", "7900", "7800 xt")) return 750;
        return 650;
    }

    private static int wattage(Product psu) {
        Matcher matcher = WATT_PATTERN.matcher(productText(psu));
        int highest = 0;
        while (matcher.find()) highest = Math.max(highest, Integer.parseInt(matcher.group(1)));
        return highest;
    }

    private static boolean hasIntegratedGraphics(String cpuText) {
        if (cpuText.contains("intel")) return !cpuText.matches(".*\\b\\w*f\\b.*");
        if (cpuText.matches(".*\\d{4}g\\b.*")) return true;
        Matcher matcher = RYZEN_MODEL_PATTERN.matcher(cpuText);
        return matcher.find() && Integer.parseInt(matcher.group(1)) >= 7000 && !cpuText.matches(".*\\d{4}f\\b.*");
    }

    private static int ramCapacity(Product ram) {
        String text = productText(ram);
        for (int size : new int[]{256, 192, 128, 96, 64, 48, 32, 24, 16, 8}) {
            if (text.contains(size + "gb")) return size;
        }
        return 0;
    }

    private static String formatAnswer(Profile profile, Candidate candidate, int optionNumber, int optionCount) {
        StringBuilder answer = new StringBuilder();
        answer.append("CẤU HÌNH NGẪU NHIÊN ").append(optionNumber).append('/').append(optionCount).append('\n')
                .append("Tầm giá: ").append(profile.priceLabel()).append('\n')
                .append("Dùng tốt cho: ").append(profile.purpose()).append("\n\n")
                .append("Linh kiện đang có trong catalog:\n");
        for (Product product : candidate.parts()) {
            answer.append("- ").append(partLabel(product.getCatId())).append(": ")
                    .append(product.getProductName()).append(" - ")
                    .append(formatPrice(product.getPrice())).append(" (kho ")
                    .append(product.getStockQty()).append(")\n");
        }
        answer.append("\nTổng giá hiện tại: ").append(formatPrice(candidate.total())).append("\n\n")
                .append("Lưu ý:\n")
                .append("- ").append(profile.note()).append('\n')
                .append("- Đã kiểm tra nền tảng CPU-mainboard, chuẩn RAM và công suất nguồn theo dữ liệu sản phẩm.\n")
                .append("- Giá và tồn kho lấy trực tiếp từ catalog, có thể thay đổi khi shop cập nhật.\n")
                .append("- Bấm lại nút này để random một trong 7 phương án khác cùng tầm giá.");
        return answer.toString();
    }

    private static String formatBudgetAnswer(Profile profile, Candidate candidate) {
        StringBuilder answer = new StringBuilder();
        answer.append("CẤU HÌNH ĐÚNG NGÂN SÁCH\n")
                .append("Ngân sách: ").append(profile.priceLabel()).append('\n')
                .append("Dùng tốt cho: ").append(profile.purpose()).append("\n\n")
                .append("Linh kiện đang có trong catalog:\n");
        for (Product product : candidate.parts()) {
            answer.append("- ").append(partLabel(product.getCatId())).append(": ")
                    .append(product.getProductName()).append(" - ")
                    .append(formatPrice(product.getPrice())).append(" (kho ")
                    .append(product.getStockQty()).append(")\n");
        }
        answer.append("\nTổng giá: ").append(formatPrice(candidate.total())).append('\n')
                .append("Còn lại: ").append(formatPrice(profile.target().subtract(candidate.total()))).append("\n\n")
                .append("Kiểm tra tương thích:\n")
                .append("- CPU-mainboard, chuẩn RAM và công suất nguồn phù hợp theo dữ liệu sản phẩm.\n")
                .append("- ").append(profile.note()).append('\n')
                .append("- Giá và tồn kho lấy trực tiếp từ catalog hiện tại.");
        return answer.toString();
    }
    private static List<Product> parts(Product cpu, Product board, Product ram, Product gpu,
                                       Product ssd, Product psu, Product cooler, Product pcCase) {
        List<Product> products = new ArrayList<>(8);
        Collections.addAll(products, cpu, board, ram);
        if (gpu != null) products.add(gpu);
        Collections.addAll(products, ssd, psu, cooler, pcCase);
        return List.copyOf(products);
    }

    private static BigDecimal total(List<Product> parts) {
        return parts.stream().map(Product::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal desired(Profile profile, int categoryId) {
        double share = switch (profile.type()) {
            case OFFICE -> switch (categoryId) { case 1 -> .24; case 2 -> .20; case 3 -> .10; case 5 -> .13; case 6 -> .09; case 7 -> .12; case 8 -> .11; default -> 0; };
            case GAMING_FHD -> switch (categoryId) { case 1 -> .18; case 2 -> .15; case 3 -> .07; case 4 -> .30; case 5 -> .09; case 6 -> .07; case 7 -> .08; case 8 -> .07; default -> 0; };
            case GAMING_QHD -> switch (categoryId) { case 1 -> .18; case 2 -> .14; case 3 -> .06; case 4 -> .34; case 5 -> .08; case 6 -> .07; case 7 -> .07; case 8 -> .06; default -> 0; };
            case CREATOR -> switch (categoryId) { case 1 -> .18; case 2 -> .13; case 3 -> .09; case 4 -> .34; case 5 -> .08; case 6 -> .06; case 7 -> .06; case 8 -> .06; default -> 0; };
            case GAMING_4K -> switch (categoryId) { case 1 -> .17; case 2 -> .14; case 3 -> .07; case 4 -> .43; case 5 -> .06; case 6 -> .07; case 7 -> .06; case 8 -> .05; default -> 0; };
            case WORKSTATION -> switch (categoryId) { case 1 -> .18; case 2 -> .13; case 3 -> .09; case 4 -> .37; case 5 -> .07; case 6 -> .055; case 7 -> .065; case 8 -> .04; default -> 0; };
            case AI -> switch (categoryId) { case 1 -> .14; case 2 -> .10; case 3 -> .07; case 4 -> .52; case 5 -> .055; case 6 -> .04; case 7 -> .045; case 8 -> .03; default -> 0; };
        };
        return profile.target().multiply(BigDecimal.valueOf(share));
    }

    private static String productText(Product product) {
        return String.join(" ", safe(product.getProductName()), safe(product.getBrand()), safe(product.getModel()),
                safe(product.getDescription()), safe(product.getSpecs())).toLowerCase(Locale.ROOT);
    }

    private static boolean containsAny(String text, String... needles) {
        for (String needle : needles) if (text.contains(needle)) return true;
        return false;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String partLabel(int categoryId) {
        return switch (categoryId) {
            case 1 -> "CPU";
            case 2 -> "Mainboard";
            case 3 -> "RAM";
            case 4 -> "GPU";
            case 5 -> "SSD";
            case 6 -> "Nguồn";
            case 7 -> "Tản nhiệt";
            case 8 -> "Case";
            default -> "Linh kiện";
        };
    }

    private static String formatPrice(BigDecimal price) {
        return price == null ? "Liên hệ" : NumberFormat.getIntegerInstance(VIETNAM).format(price.longValue()) + "đ";
    }

    private static Map<String, Profile> createProfiles() {
        Map<String, Profile> profiles = new LinkedHashMap<>();
        profiles.put("office-31", profile(UseCase.OFFICE, 31, 16, "Văn phòng, học tập, lập trình, bán hàng và đa nhiệm hằng ngày.", "Cấu hình dùng đồ họa tích hợp để ưu tiên CPU, RAM và SSD; có thể gắn thêm GPU sau."));
        profiles.put("gaming-fhd-44", profile(UseCase.GAMING_FHD, 44, 32, "Gaming Full HD, esport FPS cao và livestream cơ bản.", "GPU được ưu tiên cho 1080p; nguồn còn khoảng nâng cấp và RAM tối thiểu 32GB."));
        profiles.put("gaming-qhd-50", profile(UseCase.GAMING_QHD, 50, 32, "Gaming 1440p, game AAA thiết lập cao và màn hình tần số quét cao.", "Cân bằng CPU-GPU cho QHD, tránh dồn ngân sách quá nhiều vào một linh kiện."));
        profiles.put("creator-65", profile(UseCase.CREATOR, 65, 64, "Premiere, After Effects, Blender, Photoshop và dựng video 4K.", "Ưu tiên GPU NVIDIA/CUDA, RAM từ 64GB và SSD nhanh cho file dự án."));
        profiles.put("gaming-4k-82", profile(UseCase.GAMING_4K, 82, 64, "Gaming 4K, ray tracing, VR và livestream chất lượng cao.", "GPU cao cấp đi cùng nguồn đủ công suất; nên dùng màn hình 4K có VRR."));
        profiles.put("workstation-100", profile(UseCase.WORKSTATION, 100, 64, "Render 3D, dựng phim 4K-8K, CAD và chạy nhiều tác vụ chuyên nghiệp.", "Ưu tiên CPU nhiều nhân, GPU NVIDIA, RAM lớn và tản nhiệt phù hợp tải dài."));
        profiles.put("ai-135", profile(UseCase.AI, 135, 96, "LLM cục bộ, sinh ảnh/video AI, machine learning và CUDA chuyên sâu.", "RTX 5090 là linh kiện trọng tâm; cần nguồn tối thiểu 1000W và luồng gió tốt."));
        return Map.copyOf(profiles);
    }

    private static Profile profile(UseCase type, long millions, int minRamGb, String purpose, String note) {
        return new Profile(type, BigDecimal.valueOf(millions * 1_000_000L), "khoảng " + millions + " triệu", minRamGb,
                type != UseCase.OFFICE, purpose, note);
    }

    public record Advice(String key, String text, int optionCount) {
        private static Advice empty() { return new Advice("", "", 0); }
    }

    private enum UseCase { OFFICE, GAMING_FHD, GAMING_QHD, CREATOR, GAMING_4K, WORKSTATION, AI }
    private record Profile(UseCase type, BigDecimal target, String priceLabel, int minRamGb,
                           boolean gpuRequired, String purpose, String note) { }
    private record Candidate(String key, String coreKey, List<Product> parts, BigDecimal total,
                             BigDecimal distance) { }
}
