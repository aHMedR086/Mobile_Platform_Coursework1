package com.example.anwar_ahmed_s2534054.Situations;

import com.example.anwar_ahmed_s2534054.R;

public enum SituationType {
    ROADWORKS("Roadworks", R.drawable.ic_type_roadworks, R.color.type_roadworks),
    ROAD_CLOSURE("Road closure", R.drawable.no_entry_icon, R.color.type_closure),
    LANE_RESTRICTION("Lane restriction", R.drawable.ic_type_lanes, R.color.type_lanes),
    TRAFFIC("Abnormal traffic", R.drawable.ic_type_traffic, R.color.type_traffic),
    OBSTRUCTION("Obstruction", R.drawable.green_status, R.color.type_obstruction),
    INCIDENT("Incident", R.drawable.ic_type_incident, R.color.type_incident);

    private final String label;
    private final int iconRes;
    private final int colorRes;

    SituationType(String label, int iconRes, int colorRes) {
        this.label = label;
        this.iconRes = iconRes;
        this.colorRes = colorRes;
    }

    public String getLabel() { return label; }
    public int getIconRes() { return iconRes; }
    public int getColorRes() { return colorRes; }
}
