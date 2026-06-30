package com.pcms.categoryservice.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utility tạo URL-friendly slug từ tiếng Việt.
 * Bỏ dấu, lowercase, thay space/ký tự đặc biệt bằng dấu gạch ngang.
 */
public final class SlugUtil {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern EDGE_DASHES = Pattern.compile("^-+|-+$");

    private SlugUtil() {
    }

    /**
     * Sinh slug cơ bản từ tên hiển thị.
     * Ví dụ: "Thuốc giảm đau" -> "thuoc-giam-dau".
     */
    public static String slugify(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String noDiacritics = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String lower = noDiacritics.toLowerCase();
        String dashed = WHITESPACE.matcher(lower).replaceAll("-");
        String cleaned = NON_LATIN.matcher(dashed).replaceAll("");
        return EDGE_DASHES.matcher(cleaned).replaceAll("");
    }
}
