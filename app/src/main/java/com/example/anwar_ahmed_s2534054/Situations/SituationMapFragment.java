package com.example.anwar_ahmed_s2534054.Situations;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.anwar_ahmed_s2534054.DataObjects.PlannedRWData;
import com.example.anwar_ahmed_s2534054.DataObjects.UnplannedRWData;
import com.example.anwar_ahmed_s2534054.MapBuilder.MarkerIcons;
import com.example.anwar_ahmed_s2534054.R;
import com.example.anwar_ahmed_s2534054.Utility.SituationUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class SituationMapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "SituationMapFragment";

    private static final LatLng SCOTLAND = new LatLng(56.8, -4.2);

    private SituationsViewModel viewModel;
    private GoogleMap googleMap;
    private boolean cameraFitPending = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_situation_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SituationsViewModel.class);

        final SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googleMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        viewModel.getResultsChanged().observe(getViewLifecycleOwner(), changed -> updateMarkers());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        if (getView() == null) return;
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        googleMap.setOnInfoWindowClickListener(marker -> {
            final SituationNavigator navigator = (SituationNavigator) requireActivity();
            final Object tag = marker.getTag();
            if (tag instanceof PlannedRWData) {
                navigator.showPlannedDetail((PlannedRWData) tag);
            } else if (tag instanceof UnplannedRWData) {
                navigator.showUnplannedDetail((UnplannedRWData) tag);
            }
        });
        googleMap.setOnMapLoadedCallback(this::fitCameraIfPending);

        updateMarkers();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && getView() != null) {
            getView().post(this::fitCameraIfPending);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        googleMap = null;
    }

    private void updateMarkers() {
        if (googleMap == null) return;
        googleMap.clear();

        final LatLng ref = SituationUtils.toLatLng(SituationsViewModel.USER_LOCATION);
        googleMap.addMarker(new MarkerOptions()
                .position(ref)
                .title(getString(R.string.location_user_name))
                .snippet("Your location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        if (viewModel.getMaxDistanceKm() > 0) {
            googleMap.addCircle(new CircleOptions()
                    .center(ref)
                    .radius(viewModel.getMaxDistanceKm() * 1000)
                    .strokeWidth(2f)
                    .strokeColor(ContextCompat.getColor(requireContext(), R.color.reference_location))
                    .fillColor(ContextCompat.getColor(requireContext(), R.color.reference_radius_fill)));
        }

        for (final UnplannedRWData event : viewModel.getFilteredUnplanned()) {
            if (!event.hasLocation()) continue;
            final Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(SituationUtils.toLatLng(event.getMidpoint()))
                    .title(event.getTypeLabel())
                    .snippet(event.getDescription())
                    .icon(MarkerIcons.forIcon(requireContext(), event.getIconRes()))
                    .anchor(0.5f, 0.5f));
            if (marker != null) marker.setTag(event);
        }

        for (final PlannedRWData roadworks : viewModel.getFilteredPlanned()) {
            if (!roadworks.hasLocation()) continue;
            final Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(SituationUtils.toLatLng(roadworks.getMidpoint()))
                    .title(roadworks.getTypeLabel())
                    .snippet(roadworks.getDescription())
                    .icon(MarkerIcons.forIcon(requireContext(), roadworks.getIconRes()))
                    .anchor(0.5f, 0.5f));
            if (marker != null) marker.setTag(roadworks);
        }

        cameraFitPending = true;
        fitCameraIfPending();
    }

    private void fitCameraIfPending() {
        final View view = getView();
        if (googleMap == null || !cameraFitPending || view == null) return;
        if (isHidden() || view.getWidth() == 0 || view.getHeight() == 0) {
            return;
        }
        cameraFitPending = false;

        final LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        int count = 0;
        for (final UnplannedRWData event : viewModel.getFilteredUnplanned()) {
            if (event.hasLocation()) {
                bounds.include(SituationUtils.toLatLng(event.getMidpoint()));
                count++;
            }
        }
        for (final PlannedRWData roadworks : viewModel.getFilteredPlanned()) {
            if (roadworks.hasLocation()) {
                bounds.include(SituationUtils.toLatLng(roadworks.getMidpoint()));
                count++;
            }
        }
        if (viewModel.getMaxDistanceKm() > 0) {
            bounds.include(SituationUtils.toLatLng(SituationsViewModel.USER_LOCATION));
            count++;
        }

        try {
            if (count == 0) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SCOTLAND, 6f));
            } else if (count == 1) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.build().getCenter(), 12f));
            } else {
                final int padding = Math.round(48 * getResources().getDisplayMetrics().density);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), padding));
            }
        } catch (IllegalStateException e) {
            Log.w(TAG, "Map not ready for camera update", e);
            cameraFitPending = true;
        }
    }
}
