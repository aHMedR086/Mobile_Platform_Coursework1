package com.example.anwar_ahmed_s2534054.Situations;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.anwar_ahmed_s2534054.DataObjects.PlannedRWData;
import com.example.anwar_ahmed_s2534054.DataObjects.UnplannedRWData;
import com.example.anwar_ahmed_s2534054.MapBuilder.RoadPathFetcher;
import com.example.anwar_ahmed_s2534054.MapBuilder.SituationIcons;
import com.example.anwar_ahmed_s2534054.R;
import com.example.anwar_ahmed_s2534054.Utility.DateTimeUtils;
import com.example.anwar_ahmed_s2534054.Utility.Formatters;
import com.example.anwar_ahmed_s2534054.Utility.SituationUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class SituationDetailFragment extends Fragment {
    private LinearLayout rows;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_situation_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rows = view.findViewById(R.id.detailRows);

        final SituationsViewModel viewModel = new ViewModelProvider(requireActivity()).get(SituationsViewModel.class);
        final PlannedRWData planned = viewModel.getSelectedPlanned();
        final UnplannedRWData unplanned = viewModel.getSelectedUnplanned();

        if (planned != null) {
            showPlanned(view, planned);
        } else if (unplanned != null) {
            showUnplanned(view, unplanned);
        } else {
            getParentFragmentManager().popBackStack();
        }
    }

    private void showPlanned(View view, PlannedRWData rw) {
        showHeader(view, rw.getIconRes(), rw.getType(),
                rw.getTypeLabel(), rw.getDescription());

        final TextView duration = view.findViewById(R.id.detailDuration);
        final DurationCategory category = rw.getDurationCategory();
        duration.setText(category.getLabel() + " duration · "
                + DateTimeUtils.formatDuration(rw.getStartTime(), rw.getEndTime()));
        duration.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), category.getColorRes())));
        duration.setVisibility(View.VISIBLE);

        addRow("Road", rw.getRoadName());
        addRow("Direction", Formatters.direction(rw.getDirection()));
        addRow("From", rw.getFromDescription());
        addRow("To", rw.getToDescription());
        addRow("Starts", DateTimeUtils.formatDateTime(rw.getStartTime()));
        addRow("Ends", DateTimeUtils.formatDateTime(rw.getEndTime()));
        addRow("Expected delays", Formatters.delays(rw.getDelaysType()));
        addRow("Urgent roadworks", rw.isUrgentRoadworks() ? "Yes" : "No");
        addRow("Maintenance type", Formatters.camelCaseToWords(rw.getRoadMaintenanceType()));
        addRow("Last updated", DateTimeUtils.formatDateTime(rw.getVersionTime()));

        if (rw.hasLocation()) {
            showMap(rw.getFromLocation(), rw.getToLocation(), rw.getType());
        } else {
            hideMap();
        }
    }

    private void showUnplanned(View view, UnplannedRWData ev) {
        showHeader(view, ev.getIconRes(), ev.getType(),
                ev.getTypeLabel(), ev.getDescription());

        view.findViewById(R.id.detailDuration).setVisibility(View.GONE);

        addRow("Road", ev.getRoadName());
        addRow("Direction", Formatters.direction(ev.getDirection()));
        addRow("From", describePoint(ev.getFromIntersectionName(), ev.getFromJunctionName(), ev.getFromCrossRoadName()));
        addRow("To", describePoint(ev.getToIntersectionName(), ev.getToJunctionName(), ev.getToCrossRoadName()));
        addRow("Started", DateTimeUtils.formatDateTime(ev.getStartTime()));
        if (ev.getEndTime() != null) {
            addRow("Ends", DateTimeUtils.formatDateTime(ev.getEndTime()));
        }
        if (ev.getNumberOfOperationalLanes() != null) {
            final int lanes = ev.getNumberOfOperationalLanes();
            addRow("Lanes open", lanes == 0 ? "None (fully closed)" : String.valueOf(lanes));
        }
        if (ev.getNumberOfLanesRestricted() != null) {
            addRow("Lanes restricted", String.valueOf(ev.getNumberOfLanesRestricted()));
        }
        if (ev.getManagementType() != null) {
            addRow("Restriction", Formatters.camelCaseToWords(ev.getManagementType()));
        }
        if (ev.getComplianceOption() != null) {
            addRow("Compliance", Formatters.camelCaseToWords(ev.getComplianceOption()));
        }
        if (ev.getAbnormalTrafficType() != null) {
            addRow("Traffic", Formatters.camelCaseToWords(ev.getAbnormalTrafficType()));
        }
        if (ev.getDetailType() != null) {
            addRow("Details", Formatters.camelCaseToWords(ev.getDetailType()));
        }
        addRow("Last updated", DateTimeUtils.formatDateTime(ev.getVersionTime()));

        if (ev.hasLocation()) {
            showMap(ev.getFromLocation(), ev.getToLocation(), ev.getType());
        } else {
            hideMap();
        }
    }

    private String describePoint(String name, String junction, String crossRoad) {
        final StringBuilder sb = new StringBuilder(Formatters.orNA(name));
        if (junction != null && (name == null || !name.contains(junction))) {
            sb.append(" (junction ").append(junction).append(")");
        }
        if (crossRoad != null) {
            sb.append(" · crosses ").append(crossRoad);
        }
        return sb.toString();
    }

    private void showHeader(View view, int iconRes, SituationType type,
                            String typeLabel, String description) {
        final ImageView icon = view.findViewById(R.id.detailIcon);
        SituationIcons.applyTo(icon, iconRes);

        final TextView typeText = view.findViewById(R.id.detailType);
        typeText.setText(typeLabel);
        typeText.setTextColor(ContextCompat.getColor(requireContext(), type.getColorRes()));

        final TextView descriptionText = view.findViewById(R.id.detailDescription);
        descriptionText.setText(Formatters.orNA(description));
    }

    private void addRow(String label, String value) {
        final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_detail_row, rows, false);
        ((TextView) row.findViewById(R.id.rowLabel)).setText(label);
        ((TextView) row.findViewById(R.id.rowValue)).setText(Formatters.orNA(value));
        rows.addView(row);
    }

    private SupportMapFragment getMapFragment() {
        return (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.detailMap);
    }

    private void hideMap() {
        final SupportMapFragment mapFragment = getMapFragment();
        if (mapFragment != null && mapFragment.getView() != null) {
            mapFragment.getView().setVisibility(View.GONE);
        }
    }

    private void showMap(Pair<Double, Double> fromLocation, Pair<Double, Double> toLocation,
                         SituationType type) {
        final SupportMapFragment mapFragment = getMapFragment();
        if (mapFragment == null) return;

        final LatLng from = SituationUtils.toLatLng(fromLocation != null ? fromLocation : toLocation);
        final LatLng to = SituationUtils.toLatLng(toLocation != null ? toLocation : fromLocation);

        mapFragment.getMapAsync(map -> {
            if (!isAdded()) return;

            map.getUiSettings().setAllGesturesEnabled(false);
            map.getUiSettings().setMapToolbarEnabled(false);

            final int typeColor = ContextCompat.getColor(requireContext(), type.getColorRes());

            map.addMarker(new MarkerOptions().position(from).title("From")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            map.addMarker(new MarkerOptions().position(to).title("To")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            fitCamera(mapFragment, map, from, to, null);

            RoadPathFetcher.fetch(requireContext(), from, to, roadPath -> {
                if (!isAdded() || getView() == null) return;
                final PolylineOptions line = new PolylineOptions().color(typeColor).width(10f);
                if (roadPath != null) {
                    line.addAll(roadPath);
                } else {
                    line.add(from, to);
                }
                map.addPolyline(line);
                fitCamera(mapFragment, map, from, to, roadPath);
            });
        });
    }

    private void fitCamera(SupportMapFragment mapFragment, GoogleMap map,
                           LatLng from, LatLng to, @Nullable ArrayList<LatLng> roadPath) {
        final View mapView = mapFragment.getView();
        if (mapView == null) return;

        final Runnable fit = () -> {
            if (!isAdded()) return;
            if (from.equals(to)) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(from, 14f));
                return;
            }
            final LatLngBounds.Builder bounds = new LatLngBounds.Builder().include(from).include(to);
            if (roadPath != null) {
                for (final LatLng point : roadPath) bounds.include(point);
            }
            final int padding = Math.round(48 * getResources().getDisplayMetrics().density);
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    bounds.build(), mapView.getWidth(), mapView.getHeight(), padding));
        };
        if (mapView.getWidth() > 0) fit.run();
        else mapView.post(fit);
    }
}
