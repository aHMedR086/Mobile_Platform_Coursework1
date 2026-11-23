package com.example.anwar_ahmed_s2534054;

import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class APIInitialiser {
    private static final String TAG = "TrafficScotlandApi";

    private static final Pair<String, String> CLIENT_CREDENTIALS =
            new Pair<>(BuildConfig.TRAFFIC_SCOTLAND_CLIENT_ID, BuildConfig.TRAFFIC_SCOTLAND_CLIENT_KEY);

    private static final String BASE_URL = "https://datex2.trafficscotland.org/rest/2.3/publications/";
    public static final String UNPLANNED_EVENTS_URL = BASE_URL + "UnplannedEvents/Content.xml";
    public static final String CURRENT_ROADWORKS_URL = BASE_URL + "CurrentRoadworks/Content.xml";

    private static final int TIMEOUT_MS = 20000;

    public static String download(String feedUrl) throws IOException {
        final long startMs = System.currentTimeMillis();
        final HttpURLConnection connection = (HttpURLConnection) new URL(feedUrl).openConnection();
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestProperty("Authorization",
                Base64.getEncoder().encodeToString((CLIENT_CREDENTIALS.first + ":" + CLIENT_CREDENTIALS.second)
                        .getBytes(StandardCharsets.UTF_8)));

        try {
            final int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("Server returned HTTP " + code + " for " + feedUrl);
            }

            final int expectedLength = connection.getContentLength();
            final StringBuilder result = new StringBuilder(expectedLength > 0 ? expectedLength : 64 * 1024);

            try (InputStreamReader in = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                final char[] buffer = new char[8 * 1024];
                int charsRead;
                while ((charsRead = in.read(buffer)) != -1) {
                    result.append(buffer, 0, charsRead);
                }
            }

            final int start = result.indexOf("<?");
            if (start < 0) {
                throw new IOException("Response from " + feedUrl + " is not XML");
            }
            Log.d(TAG, "Downloaded " + result.length() + " chars in "
                    + (System.currentTimeMillis() - startMs) + " ms from " + feedUrl);
            return result.substring(start);
        } finally {
            connection.disconnect();
        }
    }
}
