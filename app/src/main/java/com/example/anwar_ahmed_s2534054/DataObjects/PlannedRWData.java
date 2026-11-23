package com.example.anwar_ahmed_s2534054.DataObjects;

import android.util.Pair;

import androidx.annotation.NonNull;

import com.example.anwar_ahmed_s2534054.Situations.DurationCategory;
import com.example.anwar_ahmed_s2534054.Situations.SituationType;
import com.example.anwar_ahmed_s2534054.Utility.SituationUtils;
import com.example.anwar_ahmed_s2534054.Utility.DateTimeUtils;

import java.time.OffsetDateTime;

public class PlannedRWData {
    private OffsetDateTime versionTime;

    private OffsetDateTime startTime;
    private OffsetDateTime endTime;

    private String description;

    private String delaysType;

    private String direction;

    private String roadName;

    private String fromDescription;
    private Pair<Double, Double> fromLocation;

    private String toDescription;
    private Pair<Double, Double> toLocation;

    private boolean urgentRoadworks;
    private String roadMaintenanceType;

    public SituationType getType() {
        return SituationType.ROADWORKS;
    }

    public String getTypeLabel() {
        return getType().getLabel();
    }

    public DurationCategory getDurationCategory() {
        return DurationCategory.of(startTime, endTime);
    }

    public int getIconRes() {
        final int durationIcon = getDurationCategory().getIconRes();
        return durationIcon != 0 ? durationIcon : getType().getIconRes();
    }

    public boolean hasLocation() {
        return fromLocation != null || toLocation != null;
    }

    public Pair<Double, Double> getMidpoint() {
        return SituationUtils.midpoint(fromLocation, toLocation);
    }

    public double distanceKmFrom(Pair<Double, Double> point) {
        return SituationUtils.distanceKm(point, fromLocation, toLocation);
    }

    public boolean isOnRoad(String road) {
        return SituationUtils.mentionsRoad(road, roadName, description);
    }

    public OffsetDateTime getVersionTime() { return versionTime; }
    public void setVersionTime(OffsetDateTime versionTime) { this.versionTime = versionTime; }

    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }

    public OffsetDateTime getEndTime() { return endTime; }
    public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDelaysType() { return delaysType; }
    public void setDelaysType(String delaysType) { this.delaysType = delaysType; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getRoadName() { return roadName; }
    public void setRoadName(String roadName) { this.roadName = roadName; }

    public String getFromDescription() { return fromDescription; }
    public void setFromDescription(String fromDescription) { this.fromDescription = fromDescription; }

    public Pair<Double, Double> getFromLocation() { return fromLocation; }
    public void setFromLocation(Pair<Double, Double> fromLocation) { this.fromLocation = fromLocation; }

    public String getToDescription() { return toDescription; }
    public void setToDescription(String toDescription) { this.toDescription = toDescription; }

    public Pair<Double, Double> getToLocation() { return toLocation; }
    public void setToLocation(Pair<Double, Double> toLocation) { this.toLocation = toLocation; }

    public boolean isUrgentRoadworks() { return urgentRoadworks; }
    public void setUrgentRoadworks(boolean urgentRoadworks) { this.urgentRoadworks = urgentRoadworks; }

    public String getRoadMaintenanceType() { return roadMaintenanceType; }
    public void setRoadMaintenanceType(String roadMaintenanceType) { this.roadMaintenanceType = roadMaintenanceType; }

    @NonNull
    @Override
    public String toString() {
        return "PlannedRWData{" +
                "description='" + description + '\'' +
                ", roadName='" + roadName + '\'' +
                ", direction='" + direction + '\'' +
                ", startTime='" + DateTimeUtils.formatDateTime(startTime) + '\'' +
                ", endTime='" + DateTimeUtils.formatDateTime(endTime) + '\'' +
                ", delaysType='" + delaysType + '\'' +
                ", from='" + fromDescription + "' " + fromLocation +
                ", to='" + toDescription + "' " + toLocation +
                ", urgentRoadworks=" + urgentRoadworks +
                ", roadMaintenanceType='" + roadMaintenanceType + '\'' +
                '}';
    }
}
