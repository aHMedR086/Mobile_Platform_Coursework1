package com.example.anwar_ahmed_s2534054.Utility;

import android.location.Location;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

import java.util.regex.Pattern;

public final class SituationUtils {
    public static Pair<Double, Double> midpoint(Pair<Double, Double> from, Pair<Double, Double> to) {
        if (from == null) return to;
        if (to == null) return from;
        final double latitude = (from.first + to.first) / 2;
        final double longitude = (from.second + to.second) / 2;
        return new Pair<>(latitude, longitude);
    }

    public static double distanceKm(Pair<Double, Double> point,
                                    Pair<Double, Double> from, Pair<Double, Double> to) {
        double nearestMetres = Double.MAX_VALUE;
        if (from != null) {
            nearestMetres = Math.min(nearestMetres, metresBetween(point, from));
        }
        if (to != null) {
            nearestMetres = Math.min(nearestMetres, metresBetween(point, to));
        }
        return nearestMetres / 1000.0;
    }

    private static double metresBetween(Pair<Double, Double> a, Pair<Double, Double> b) {
        final float[] result = new float[1];
        Location.distanceBetween(a.first, a.second, b.first, b.second, result);
        return result[0];
    }

    public static LatLng toLatLng(Pair<Double, Double> location) {
        return new LatLng(location.first, location.second);
    }

    public static boolean mentionsRoad(String road, String... texts) {
        if (road == null || road.trim().isEmpty()) return true;
        final Pattern pattern = Pattern.compile(
                "(?<![A-Za-z0-9])" + Pattern.quote(road.trim()) + "(?![A-Za-z0-9])",
                Pattern.CASE_INSENSITIVE);
        for (final String text : texts) {
            if (text != null && pattern.matcher(text).find()) return true;
        }
        return false;
    }
}
