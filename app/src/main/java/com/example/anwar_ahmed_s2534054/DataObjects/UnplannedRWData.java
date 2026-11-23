package com.example.anwar_ahmed_s2534054.DataObjects;

import android.util.Pair;

import androidx.annotation.NonNull;

import com.example.anwar_ahmed_s2534054.Situations.SituationType;
import com.example.anwar_ahmed_s2534054.Utility.SituationUtils;
import com.example.anwar_ahmed_s2534054.Utility.DateTimeUtils;
import com.example.anwar_ahmed_s2534054.Utility.Formatters;

import java.time.OffsetDateTime;

public class UnplannedRWData {
    private String recordType;

    private OffsetDateTime versionTime;

    private OffsetDateTime startTime;
    private OffsetDateTime endTime;

    private String description;

    private String direction;

    private Integer numberOfOperationalLanes;
    private Integer numberOfLanesRestricted;

    private String fromJunctionName;
    private String fromRoadName;
    private String fromCrossRoadName;
    private String fromIntersectionName;
    private Pair<Double, Double> fromLocation;

    private String toJunctionName;
    private String toRoadName;
    private String toCrossRoadName;
    private String toIntersectionName;
    private Pair<Double, Double> toLocation;

    private String complianceOption;
    private String managementType;

    private String abnormalTrafficType;

    private String detailType;

    public SituationType getType() {
        if (recordType == null) return SituationType.INCIDENT;
        if (recordType.equals("RoadOrCarriagewayOrLaneManagement")) {
            return "roadClosed".equalsIgnoreCase(managementType)
                    ? SituationType.ROAD_CLOSURE : SituationType.LANE_RESTRICTION;
        }
        if (recordType.equals("AbnormalTraffic")) return SituationType.TRAFFIC;
        if (recordType.endsWith("Obstruction")) return SituationType.OBSTRUCTION;
        if (recordType.endsWith("Works")) return SituationType.ROADWORKS;
        return SituationType.INCIDENT;
    }

    public String getTypeLabel() {
        if (getType() == SituationType.INCIDENT && recordType != null) {
            return Formatters.camelCaseToWords(recordType);
        }
        return getType().getLabel();
    }

    public int getIconRes() {
        return getType().getIconRes();
    }

    public String getRoadName() {
        return fromRoadName != null ? fromRoadName : toRoadName;
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
        return SituationUtils.mentionsRoad(road,
                fromRoadName, toRoadName, fromCrossRoadName, toCrossRoadName, description);
    }

    public void setRecordType(String recordType) { this.recordType = recordType; }

    public OffsetDateTime getVersionTime() { return versionTime; }
    public void setVersionTime(OffsetDateTime versionTime) { this.versionTime = versionTime; }

    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }

    public OffsetDateTime getEndTime() { return endTime; }
    public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public Integer getNumberOfOperationalLanes() { return numberOfOperationalLanes; }
    public void setNumberOfOperationalLanes(Integer numberOfOperationalLanes) { this.numberOfOperationalLanes = numberOfOperationalLanes; }

    public Integer getNumberOfLanesRestricted() { return numberOfLanesRestricted; }
    public void setNumberOfLanesRestricted(Integer numberOfLanesRestricted) { this.numberOfLanesRestricted = numberOfLanesRestricted; }

    public String getFromJunctionName() { return fromJunctionName; }
    public void setFromJunctionName(String fromJunctionName) { this.fromJunctionName = fromJunctionName; }

    public String getFromRoadName() { return fromRoadName; }
    public void setFromRoadName(String fromRoadName) { this.fromRoadName = fromRoadName; }

    public String getFromCrossRoadName() { return fromCrossRoadName; }
    public void setFromCrossRoadName(String fromCrossRoadName) { this.fromCrossRoadName = fromCrossRoadName; }

    public String getFromIntersectionName() { return fromIntersectionName; }
    public void setFromIntersectionName(String fromIntersectionName) { this.fromIntersectionName = fromIntersectionName; }

    public Pair<Double, Double> getFromLocation() { return fromLocation; }
    public void setFromLocation(Pair<Double, Double> fromLocation) { this.fromLocation = fromLocation; }

    public String getToJunctionName() { return toJunctionName; }
    public void setToJunctionName(String toJunctionName) { this.toJunctionName = toJunctionName; }

    public String getToRoadName() { return toRoadName; }
    public void setToRoadName(String toRoadName) { this.toRoadName = toRoadName; }

    public String getToCrossRoadName() { return toCrossRoadName; }
    public void setToCrossRoadName(String toCrossRoadName) { this.toCrossRoadName = toCrossRoadName; }

    public String getToIntersectionName() { return toIntersectionName; }
    public void setToIntersectionName(String toIntersectionName) { this.toIntersectionName = toIntersectionName; }

    public Pair<Double, Double> getToLocation() { return toLocation; }
    public void setToLocation(Pair<Double, Double> toLocation) { this.toLocation = toLocation; }

    public String getComplianceOption() { return complianceOption; }
    public void setComplianceOption(String complianceOption) { this.complianceOption = complianceOption; }

    public String getManagementType() { return managementType; }
    public void setManagementType(String managementType) { this.managementType = managementType; }

    public String getAbnormalTrafficType() { return abnormalTrafficType; }
    public void setAbnormalTrafficType(String abnormalTrafficType) { this.abnormalTrafficType = abnormalTrafficType; }

    public String getDetailType() { return detailType; }
    public void setDetailType(String detailType) { this.detailType = detailType; }

    @NonNull
    @Override
    public String toString() {
        return "UnplannedRWData{" +
                "recordType='" + recordType + '\'' +
                ", description='" + description + '\'' +
                ", direction='" + direction + '\'' +
                ", startTime='" + DateTimeUtils.formatDateTime(startTime) + '\'' +
                ", operationalLanes=" + numberOfOperationalLanes +
                ", lanesRestricted=" + numberOfLanesRestricted +
                ", from='" + fromIntersectionName + "' " + fromLocation +
                ", to='" + toIntersectionName + "' " + toLocation +
                ", managementType='" + managementType + '\'' +
                ", abnormalTrafficType='" + abnormalTrafficType + '\'' +
                ", detailType='" + detailType + '\'' +
                '}';
    }
}
