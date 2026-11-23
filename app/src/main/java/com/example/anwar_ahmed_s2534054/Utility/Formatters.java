package com.example.anwar_ahmed_s2534054.Utility;

import java.util.Locale;

public final class Formatters {
    public static String camelCaseToWords(String text) {
        if (text == null || text.isEmpty()) return null;
        final String spaced = text.replaceAll("([a-z0-9])([A-Z])", "$1 $2").toLowerCase(Locale.UK);
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    public static String direction(String direction) {
        if (direction == null) return null;
        if (direction.endsWith("Bound")) {
            final String d = direction.substring(0, direction.length() - "Bound".length());
            return Character.toUpperCase(d.charAt(0)) + d.substring(1) + "bound";
        }
        return camelCaseToWords(direction);
    }

    public static String delays(String delaysType) {
        if (delaysType == null) return "None reported";
        return camelCaseToWords(delaysType);
    }

    public static String distance(double km) {
        if (km < 1) return String.format(Locale.UK, "%.0f m away", km * 1000);
        return String.format(Locale.UK, "%.1f km away", km);
    }

    public static String orNA(Object value) {
        if (value == null) return "N/A";
        final String s = value.toString();
        return s.trim().isEmpty() ? "N/A" : s;
    }
}
