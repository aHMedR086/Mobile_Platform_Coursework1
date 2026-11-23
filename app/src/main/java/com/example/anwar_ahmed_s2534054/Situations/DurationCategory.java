package com.example.anwar_ahmed_s2534054.Situations;

import com.example.anwar_ahmed_s2534054.R;

import java.time.Duration;
import java.time.OffsetDateTime;

public enum DurationCategory {
    SHORT("Short", "under 1 week", R.color.duration_short, R.drawable.green_status),
    MEDIUM("Medium", "1 to 4 weeks", R.color.duration_medium, R.drawable.warning_icon),
    LONG("Long", "over 4 weeks", R.color.duration_long, R.drawable.error_icon),
    UNKNOWN("Unknown", "no end date", R.color.duration_unknown, 0);

    private static final long SHORT_MAX_DAYS = 7;
    private static final long MEDIUM_MAX_DAYS = 28;

    private final String label;
    private final String rangeText;
    private final int colorRes;
    private final int iconRes;

    DurationCategory(String label, String rangeText, int colorRes, int iconRes) {
        this.label = label;
        this.rangeText = rangeText;
        this.colorRes = colorRes;
        this.iconRes = iconRes;
    }

    public String getLabel() { return label; }
    public String getRangeText() { return rangeText; }
    public int getColorRes() { return colorRes; }
    public int getIconRes() { return iconRes; }

    public static DurationCategory of(OffsetDateTime start, OffsetDateTime end) {
        if (start == null || end == null || end.isBefore(start)) return UNKNOWN;
        final long days = Duration.between(start, end).toDays();
        if (days < SHORT_MAX_DAYS) return SHORT;
        if (days <= MEDIUM_MAX_DAYS) return MEDIUM;
        return LONG;
    }
}
