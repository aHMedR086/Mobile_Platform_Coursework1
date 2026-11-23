package com.example.anwar_ahmed_s2534054.MapBuilder;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RoadPathFetcher {
    public interface Callback {
        void onResult(ArrayList<LatLng> path);
    }

    private static final String TAG = "RoadPathFetcher";
    private static final String ROUTES_URL = "https://routes.googleapis.com/directions/v2:computeRoutes";
    private static final String SNAP_URL = "https://roads.googleapis.com/v1/snapToRoads";

    private static final double POINT_SPACING_M = 150;
    private static final int MAX_POINTS = 100;
    private static final int TIMEOUT_MS = 15000;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainThread = new Handler(Looper.getMainLooper());

    private static final Map<String, ArrayList<LatLng>> cache = new HashMap<>();

    public static void fetch(Context context, LatLng from, LatLng to, Callback callback) {
        final String id = String.format(Locale.UK, "%.6f,%.6f->%.6f,%.6f",
                from.latitude, from.longitude, to.latitude, to.longitude);
        synchronized (cache) {
            if (cache.containsKey(id)) {
                callback.onResult(cache.get(id));
                return;
            }
        }

        final String apiKey = readApiKey(context.getApplicationContext());

        executor.execute(() -> {
            ArrayList<LatLng> path = null;
            String method = "straight line";
            if (apiKey != null && !apiKey.isEmpty() && distanceM(from, to) >= 20) {
                try {
                    path = drivingRoute(from, to, apiKey);
                    if (path != null) method = "driving route";
                } catch (Exception e) {
                    Log.w(TAG, "Routes API failed for " + id, e);
                }
                if (path == null) {
                    try {
                        path = snapToRoad(from, to, apiKey);
                        if (path != null) method = "snapped to road";
                    } catch (Exception e) {
                        Log.w(TAG, "Roads API failed for " + id, e);
                    }
                }
            }
            Log.d(TAG, id + ": line drawn as " + method);
            if (path != null) {
                synchronized (cache) {
                    cache.put(id, path);
                }
            }
            final ArrayList<LatLng> result = path;
            mainThread.post(() -> callback.onResult(result));
        });
    }

    private static ArrayList<LatLng> drivingRoute(LatLng from, LatLng to, String apiKey) throws Exception {
        final String body = String.format(Locale.UK,
                "{\"origin\":{\"location\":{\"latLng\":{\"latitude\":%.6f,\"longitude\":%.6f}}},"
                + "\"destination\":{\"location\":{\"latLng\":{\"latitude\":%.6f,\"longitude\":%.6f}}},"
                + "\"travelMode\":\"DRIVE\",\"routingPreference\":\"TRAFFIC_UNAWARE\","
                + "\"polylineEncoding\":\"GEO_JSON_LINESTRING\"}",
                from.latitude, from.longitude, to.latitude, to.longitude);

        final HttpURLConnection connection = (HttpURLConnection) new URL(ROUTES_URL).openConnection();
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-Goog-Api-Key", apiKey);
        connection.setRequestProperty("X-Goog-FieldMask", "routes.polyline.geoJsonLinestring");
        try {
            connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "Routes API returned HTTP " + connection.getResponseCode());
                return null;
            }
            final JSONArray routes = new JSONObject(readBody(connection)).optJSONArray("routes");
            if (routes == null || routes.length() == 0) return null;

            final JSONArray coords = routes.getJSONObject(0).getJSONObject("polyline")
                    .getJSONObject("geoJsonLinestring").getJSONArray("coordinates");
            ArrayList<LatLng> path = new ArrayList<>();
            for (int i = 0; i < coords.length(); i++) {
                final JSONArray c = coords.getJSONArray(i);
                path.add(new LatLng(c.getDouble(1), c.getDouble(0)));
            }

            final double straightM = distanceM(from, to);
            return looksRight(path, from, to, straightM * 1.5 + 400) ? path : null;
        } finally {
            connection.disconnect();
        }
    }

    private static ArrayList<LatLng> snapToRoad(LatLng from, LatLng to, String apiKey) throws Exception {
        final double straightM = distanceM(from, to);

        final int count = (int) Math.min(MAX_POINTS, Math.max(2, Math.ceil(straightM / POINT_SPACING_M) + 1));
        final StringBuilder pathParam = new StringBuilder();
        for (int i = 0; i < count; i++) {
            final double f = (double) i / (count - 1);
            final double lat = from.latitude + (to.latitude - from.latitude) * f;
            final double lon = from.longitude + (to.longitude - from.longitude) * f;
            if (i > 0) pathParam.append('|');
            pathParam.append(String.format(Locale.UK, "%.6f,%.6f", lat, lon));
        }

        final URL url = new URL(SNAP_URL + "?interpolate=true&path=" + pathParam + "&key=" + apiKey);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        try {
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "Roads API returned HTTP " + connection.getResponseCode());
                return null;
            }
            final JSONArray snapped = new JSONObject(readBody(connection)).optJSONArray("snappedPoints");
            if (snapped == null || snapped.length() < 2) return null;

            ArrayList<LatLng> path = new ArrayList<>();
            for (int i = 0; i < snapped.length(); i++) {
                final JSONObject loc = snapped.getJSONObject(i).getJSONObject("location");
                path.add(new LatLng(loc.getDouble("latitude"), loc.getDouble("longitude")));
            }

            return looksRight(path, from, to, straightM * 1.3 + 100) ? path : null;
        } finally {
            connection.disconnect();
        }
    }

    private static boolean looksRight(ArrayList<LatLng> path, LatLng from, LatLng to, double maxLengthM) {
        final double straightM = distanceM(from, to);
        final double endTolerance = Math.max(150, straightM * 0.2);
        if (distanceM(path.get(0), from) > endTolerance) return false;
        if (distanceM(path.get(path.size() - 1), to) > endTolerance) return false;

        double length = 0;
        for (int i = 1; i < path.size(); i++) {
            length += distanceM(path.get(i - 1), path.get(i));
        }
        return length >= straightM * 0.7 && length <= maxLengthM;
    }

    private static String readBody(HttpURLConnection connection) throws Exception {
        final StringBuilder body = new StringBuilder();
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) body.append(line);
        }
        return body.toString();
    }

    private static double distanceM(LatLng a, LatLng b) {
        final float[] result = new float[1];
        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, result);
        return result[0];
    }

    private static String readApiKey(Context context) {
        try {
            final ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            final Bundle metaData = info.metaData;
            return metaData != null ? metaData.getString("com.google.android.geo.API_KEY") : null;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
