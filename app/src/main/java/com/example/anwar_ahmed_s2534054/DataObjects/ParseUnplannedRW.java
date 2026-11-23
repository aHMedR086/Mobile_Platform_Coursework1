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

public class ParseUnplannedRW {
    private static final String TAG = "ParseUnplannedRW";
    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";

    public ArrayList<UnplannedRWData> parseData(String dataToParse) {
        final ArrayList<UnplannedRWData> eventList = new ArrayList<>();
        UnplannedRWData event = null;

        boolean inComment = false;
        boolean inFrom = false;
        boolean inTo = false;
        String lastValue = null;
        double latitude = 0;
        int recordDepth = -1;

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
                        event = new UnplannedRWData();
                        event.setRecordType(xpp.getAttributeValue(XSI_NAMESPACE, "type"));
                        recordDepth = xpp.getDepth();
                    }
                    else if (event == null) {

                    }
                    else if (tag.equalsIgnoreCase("situationRecordVersionTime")) {
                        event.setVersionTime(DateTimeUtils.parse(xpp.nextText()));
                    }
                    else if (tag.equalsIgnoreCase("overallStartTime")) {
                        event.setStartTime(DateTimeUtils.parse(xpp.nextText()));
                    }
                    else if (tag.equalsIgnoreCase("overallEndTime")) {
                        event.setEndTime(DateTimeUtils.parse(xpp.nextText()));
                    }
                    else if (tag.equalsIgnoreCase("numberOfOperationalLanes")) {
                        event.setNumberOfOperationalLanes(parseInt(xpp.nextText()));
                    }
                    else if (tag.equalsIgnoreCase("numberOfLanesRestricted")) {
                        event.setNumberOfLanesRestricted(parseInt(xpp.nextText()));
                    }
                    else if (tag.equalsIgnoreCase("generalPublicComment")) {
                        inComment = true;
                    }
                    else if (tag.equalsIgnoreCase("tpegDirection")) {
                        event.setDirection(xpp.nextText());
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
                        if (inFrom) event.setFromLocation(location);
                        else if (inTo) event.setToLocation(location);
                    }
                    else if (tag.equalsIgnoreCase("value")) {
                        final String text = xpp.nextText();
                        if (inComment) {
                            event.setDescription(text);
                        } else if (inFrom || inTo) {
                            lastValue = text;
                        }
                    }
                    else if (tag.equalsIgnoreCase("tpegJunctionPointDescriptorType")) {
                        final String type = xpp.nextText();
                        if (type.equalsIgnoreCase("junctionName")) {
                            if (inFrom) event.setFromJunctionName(lastValue);
                            else if (inTo) event.setToJunctionName(lastValue);
                        }
                        lastValue = null;
                    }
                    else if (tag.equalsIgnoreCase("tpegIlcPointDescriptorType")) {
                        final String type = xpp.nextText();
                        if (type.equalsIgnoreCase("tpegIlcName1")) {
                            if (inFrom) event.setFromRoadName(lastValue);
                            else if (inTo) event.setToRoadName(lastValue);
                        } else if (type.equalsIgnoreCase("tpegIlcName2")) {
                            if (inFrom) event.setFromCrossRoadName(lastValue);
                            else if (inTo) event.setToCrossRoadName(lastValue);
                        }
                        lastValue = null;
                    }
                    else if (tag.equalsIgnoreCase("tpegOtherPointDescriptorType")) {
                        final String type = xpp.nextText();
                        if (type.equalsIgnoreCase("intersectionName")
                                || type.equalsIgnoreCase("nonLinkedPointName")) {
                            if (inFrom) event.setFromIntersectionName(lastValue);
                            else if (inTo) event.setToIntersectionName(lastValue);
                        } else if (type.equalsIgnoreCase("linkName")) {
                            if (inFrom && event.getFromRoadName() == null) event.setFromRoadName(lastValue);
                            else if (inTo && event.getToRoadName() == null) event.setToRoadName(lastValue);
                        }
                        lastValue = null;
                    }
                    else if (tag.equalsIgnoreCase("complianceOption")) {
                        event.setComplianceOption(xpp.nextText());
                    }
                    else if (tag.equalsIgnoreCase("roadOrCarriagewayOrLaneManagementType")) {
                        event.setManagementType(xpp.nextText());
                    }
                    else if (tag.equalsIgnoreCase("abnormalTrafficType")) {
                        event.setAbnormalTrafficType(xpp.nextText());
                    }
                    else if (xpp.getDepth() == recordDepth + 1 && tag.endsWith("Type")) {
                        event.setDetailType(xpp.nextText());
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
                    else if (tag.equalsIgnoreCase("situationRecord") && event != null) {
                        eventList.add(event);
                        event = null;
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

        Log.d(TAG, "Parsed " + eventList.size() + " unplanned events");
        return eventList;
    }

    private double parseDouble(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid coordinate: " + text);
            return 0.0;
        }
    }

    private Integer parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid number: " + text);
            return null;
        }
    }
}
