package com.example.anwar_ahmed_s2534054.Utility;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateTimeUtils {
    public static String formatDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }

        final ZoneId ukZone = ZoneId.of("Europe/London");
        final ZonedDateTime ukDateTime = dateTime.atZoneSameInstant(ukZone);

        final DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("EEE d MMM yyyy, HH:mm", Locale.UK);
        return ukDateTime.format(displayFormat);
    }

    public static OffsetDateTime parse(String text) {
        return OffsetDateTime.parse(text.trim());
    }

    public static String formatDuration(OffsetDateTime start, OffsetDateTime end) {
        if (start == null || end == null || end.isBefore(start)) {
            return "no end date";
        }
        final long hours = Duration.between(start, end).toHours();
        final long days = hours / 24;
        if (hours < 24) return plural(Math.max(hours, 1), "hour");
        if (days < 14) return plural(days, "day");
        if (days < 60) return plural(days / 7, "week");
        if (days < 730) return plural(days / 30, "month");
        return plural(days / 365, "year");
    }

    private static String plural(long n, String unit) {
        return n + " " + unit + (n == 1 ? "" : "s");
    }
}
