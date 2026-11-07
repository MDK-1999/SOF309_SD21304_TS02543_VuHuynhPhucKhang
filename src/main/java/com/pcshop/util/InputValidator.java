package com.pcshop.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class InputValidator {
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._-]{4,32}");
    private static final Pattern EMAIL = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE = Pattern.compile("^(?:\\+84|0)[0-9]{9,10}$");
    private static final Set<String> ROLES = Set.of("admin", "staff", "customer");

    private InputValidator() {}

    public static String text(String value, int maxLength) {
        if (value == null) return "";
        String cleaned = value.replace('\u0000', ' ')
                .replace('<', ' ')
                .replace('>', ' ')
                .replace('\'', '\u2019')
                .replace('"', '\u201D')
                .replaceAll("[\\p{Cntrl}&&[^\\t]]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return limit(cleaned, maxLength);
    }

    public static String multiline(String value, int maxLength) {
        if (value == null) return "";
        String cleaned = value.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\u0000', ' ')
                .replace('<', ' ')
                .replace('>', ' ')
                .replace('\'', '\u2019')
                .replace('"', '\u201D')
                .replaceAll("[\\p{Cntrl}&&[^\\n\\t]]", " ")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
        return limit(cleaned, maxLength);
    }

    public static boolean username(String value) {
        return value != null && USERNAME.matcher(value.trim()).matches();
    }

    public static boolean email(String value) {
        return value == null || value.isBlank() || EMAIL.matcher(value.trim()).matches();
    }

    public static boolean phone(String value) {
        if (value == null || value.isBlank()) return false;
        String normalized = value.replaceAll("[ .()-]", "");
        return PHONE.matcher(normalized).matches();
    }

    public static String normalizePhone(String value) {
        return value == null ? "" : value.replaceAll("[ .()-]", "").trim();
    }

    public static boolean strongPassword(String value) {
        if (value == null || value.length() < 8 || !fitsBcrypt(value)) return false;
        boolean upper = false, lower = false, digit = false;
        for (char character : value.toCharArray()) {
            upper |= Character.isUpperCase(character);
            lower |= Character.isLowerCase(character);
            digit |= Character.isDigit(character);
        }
        return upper && lower && digit;
    }

    public static boolean fitsBcrypt(String value) {
        return value != null && value.length() <= 72 && value.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    public static int positiveInt(String value, int fallback, int maximum) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 && parsed <= maximum ? parsed : fallback;
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    public static int nonNegativeInt(String value, int fallback, int maximum) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed >= 0 && parsed <= maximum ? parsed : fallback;
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    public static BigDecimal money(String value, BigDecimal fallback, BigDecimal maximum) {
        try {
            BigDecimal parsed = new BigDecimal(value);
            return parsed.signum() >= 0 && parsed.compareTo(maximum) <= 0 ? parsed : fallback;
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    public static String role(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return ROLES.contains(normalized) ? normalized : "customer";
    }

    private static String limit(String value, int maximum) {
        return value.length() <= maximum ? value : value.substring(0, maximum);
    }
}
