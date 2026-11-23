package com.example.anwar_ahmed_s2534054.DataObjects;

import android.util.Log;
import android.util.Pair;

import com.example.anwar_ahmed_s2534054.Utility.DateTimeUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class ParsePlannedRW {
    private static final String TAG = "ParsePlannedRW";

    public ArrayList<PlannedRWData> parseData(String dataToParse) {
        final ArrayList<PlannedRWData> roadworksList = new ArrayList<>();
        PlannedRWData roadworks = null;

        boolean inComment = false;
        boolean inFrom = false;
        boolean inTo = false;
        String lastValue = null;
        double latitude = 0;

        try {
            final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            final XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(dataToParse));

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG)
                {
                    final String tag = xpp.getName();

                    if (tag.equalsIgnoreCase("situationRecord")) {
                        roadworks = new PlannedRWData();
                    }
                    else if (roadworks == null) {

                    }
                    else if (tag.equalsIgnoreCase("situationRecordVersionTime")) {
                        roadworks.setVersionTime(DateTimeUtils.parse(xpp.nextText()));
                    }
                    else if (tag.equalsIgnoreCase("overallStartTime")) {
                        roadworks.setStartTime(DateTimeUtils.parse(xpp.nextText()));
                    }
                    else if (tag.equalsIgnoreCase("overallEndTime")) {
                        roadworks.setEndTime(DateTimeUtils.parse(xpp.nextText()));
                    }
                    else if (tag.equalsIgnoreCase("delaysType")) {
                        roadworks.setDelaysType(xpp.nextText());
                    }
                    else if (tag.equalsIgnoreCase("generalPublicComment")) {
                        inComment = true;
                    }
                    else if (tag.equalsIgnoreCase("tpegDirection")) {
                        roadworks.setDirection(xpp.nextText());
                    }
                    else if (tag.equalsIgnoreCase("from")) {
                        inFrom = true;
                    }
                    else if (tag.equalsIgnoreCase("to")) {
                        inTo = true;
                    }
                    else if (tag.equalsIgnoreCase("latitude")) {
                        latitude = parseDouble(xpp.nextText());
                    }
                    else if (tag.equalsIgnoreCase("longitude")) {
                        final double longitude = parseDouble(xpp.nextText());
                        final Pair<Double, Double> location = new Pair<>(latitude, longitude);
                        if (inFrom) roadworks.setFromLocation(location);
                        else if (inTo) roadworks.setToLocation(location);
                    }
                    else if (tag.equalsIgnoreCase("value")) {
                        final String text = xpp.nextText();
                        if (inComment) {
                            roadworks.setDescription(text);
                        } else if (inFrom || inTo) {
                            lastValue = text;
                        }
                    }
                    else if (tag.equalsIgnoreCase("tpegOtherPointDescriptorType")) {
                        final String type = xpp.nextText();
                        if (type.equalsIgnoreCase("linkName")) {
                            if (roadworks.getRoadName() == null) {
                                roadworks.setRoadName(lastValue);
                            }
                        } else if (type.equalsIgnoreCase("nonLinkedPointName")) {
                            if (inFrom) roadworks.setFromDescription(lastValue);
                            else if (inTo) roadworks.setToDescription(lastValue);
                        }
                        lastValue = null;
                    }
                    else if (tag.equalsIgnoreCase("urgentRoadworks")) {
                        roadworks.setUrgentRoadworks(Boolean.parseBoolean(xpp.nextText()));
                    }
                    else if (tag.equalsIgnoreCase("roadMaintenanceType")) {
                        roadworks.setRoadMaintenanceType(xpp.nextText());
                    }
                }
                else if (eventType == XmlPullParser.END_TAG)
                {
                    final String tag = xpp.getName();

                    if (tag.equalsIgnoreCase("generalPublicComment")) {
                        inComment = false;
                    }
                    else if (tag.equalsIgnoreCase("from")) {
                        inFrom = false;
                    }
                    else if (tag.equalsIgnoreCase("to")) {
                        inTo = false;
                    }
                    else if (tag.equalsIgnoreCase("situationRecord") && roadworks != null) {
                        roadworksList.add(roadworks);
                        roadworks = null;
                    }
                }
                eventType = xpp.next();
            }
        }
        catch (XmlPullParserException e) {
            Log.e(TAG, "Parsing error " + e);
        }
        catch (IOException e) {
            Log.e(TAG, "IO error during parsing " + e);
        }

        Log.d(TAG, "Parsed " + roadworksList.size() + " planned roadworks");
        return roadworksList;
    }

    private double parseDouble(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid coordinate: " + text);
            return 0.0;
        }
    }
}
