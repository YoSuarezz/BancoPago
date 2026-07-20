package com.bancopago.backend.crosscutting.helpers;

public final class TextHelper {
    public static final String EMPTY = "";
    private TextHelper() {}

    public static String applyTrim(String value) {
        return value == null ? EMPTY : value.trim();
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static String truncate(String value, int maxLength) {
        if (value == null) return EMPTY;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
