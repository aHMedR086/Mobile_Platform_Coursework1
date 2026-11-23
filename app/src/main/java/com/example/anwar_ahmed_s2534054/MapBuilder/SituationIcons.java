package com.example.anwar_ahmed_s2534054.MapBuilder;

import android.widget.ImageView;

public final class SituationIcons {
    private static final int PADDING_DP = 2;

    public static void applyTo(ImageView view, int iconRes) {
        view.setImageResource(iconRes);
        view.setBackground(null);
        final int px = Math.round(PADDING_DP * view.getResources().getDisplayMetrics().density);
        view.setPadding(px, px, px, px);
    }
}
