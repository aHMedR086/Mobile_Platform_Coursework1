package com.example.anwar_ahmed_s2534054.Situations;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.anwar_ahmed_s2534054.DataObjects.ParsePlannedRW;
import com.example.anwar_ahmed_s2534054.DataObjects.ParseUnplannedRW;
import com.example.anwar_ahmed_s2534054.DataObjects.PlannedRWData;
import com.example.anwar_ahmed_s2534054.DataObjects.UnplannedRWData;
import com.example.anwar_ahmed_s2534054.APIInitialiser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SituationsViewModel extends ViewModel {
    private static final String TAG = "SituationsViewModel";

    public static final double[] DISTANCES = { 0, 10, 20, 50, 100 };

    public static final Pair<Double, Double> USER_LOCATION = new Pair<>(55.8665, -4.2502);

    public enum TypeFilter { ALL, ROADWORKS, INCIDENTS }

    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Handler mainThread = new Handler(Looper.getMainLooper());

    private final ArrayList<UnplannedRWData> allUnplanned = new ArrayList<>();
    private final ArrayList<PlannedRWData> allPlanned = new ArrayList<>();

    private final ArrayList<UnplannedRWData> filteredUnplanned = new ArrayList<>();
    private final ArrayList<PlannedRWData> filteredPlanned = new ArrayList<>();

    private final MutableLiveData<Boolean> resultsChanged = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    private boolean loaded = false;
    private int downloadsRemaining = 0;

    private TypeFilter typeFilter = TypeFilter.ALL;
    private String roadQuery = "";
    private double maxDistanceKm = 0;

    private boolean showingMap = false;

    private PlannedRWData selectedPlanned;
    private UnplannedRWData selectedUnplanned;

    public LiveData<Boolean> getResultsChanged() { return resultsChanged; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public ArrayList<UnplannedRWData> getFilteredUnplanned() { return filteredUnplanned; }
    public ArrayList<PlannedRWData> getFilteredPlanned() { return filteredPlanned; }
    public int getTotalCount() { return allUnplanned.size() + allPlanned.size(); }

    public double getMaxDistanceKm() { return maxDistanceKm; }

    public boolean isShowingMap() { return showingMap; }
    public void setShowingMap(boolean showingMap) { this.showingMap = showingMap; }

    public void loadIfNeeded() {
        if (!loaded) refresh();
    }

    public void refresh() {
        if (downloadsRemaining > 0) return;
        downloadsRemaining = 2;
        loading.setValue(true);
        error.setValue(null);

        executor.execute(() -> {
            try {
                final String xml = APIInitialiser.download(APIInitialiser.UNPLANNED_EVENTS_URL);
                final ArrayList<UnplannedRWData> unplanned = new ParseUnplannedRW().parseData(xml);
                mainThread.post(() -> {
                    allUnplanned.clear();
                    allUnplanned.addAll(unplanned);
                    downloadFinished(true);
                });
            } catch (IOException e) {
                Log.e(TAG, "Unplanned events download failed", e);
                mainThread.post(() -> downloadFinished(false));
            }
        });

        executor.execute(() -> {
            try {
                final String xml = APIInitialiser.download(APIInitialiser.CURRENT_ROADWORKS_URL);
                final ArrayList<PlannedRWData> planned = new ParsePlannedRW().parseData(xml);
                mainThread.post(() -> {
                    allPlanned.clear();
                    allPlanned.addAll(planned);
                    downloadFinished(true);
                });
            } catch (IOException e) {
                Log.e(TAG, "Roadworks download failed", e);
                mainThread.post(() -> downloadFinished(false));
            }
        });
    }

    private void downloadFinished(boolean succeeded) {
        if (!succeeded) {
            error.setValue("Could not download all the data. Check your internet connection and tap refresh.");
        }
        applyFilters();

        downloadsRemaining--;
        if (downloadsRemaining == 0) {
            loaded = error.getValue() == null;
            loading.setValue(false);
        }
    }

    public void setTypeFilter(TypeFilter filter) {
        typeFilter = filter;
        applyFilters();
    }

    public void setRoadQuery(String query) {
        roadQuery = query.trim();
        applyFilters();
    }

    public void setMaxDistanceKm(double km) {
        maxDistanceKm = km;
        applyFilters();
    }

    private void applyFilters() {
        filteredUnplanned.clear();
        if (typeFilter != TypeFilter.ROADWORKS) {
            for (final UnplannedRWData event : allUnplanned) {
                if (event.isOnRoad(roadQuery) && isWithinDistance(event.distanceKmFrom(USER_LOCATION))) {
                    filteredUnplanned.add(event);
                }
            }
        }

        filteredPlanned.clear();
        if (typeFilter != TypeFilter.INCIDENTS) {
            for (final PlannedRWData roadworks : allPlanned) {
                if (roadworks.isOnRoad(roadQuery) && isWithinDistance(roadworks.distanceKmFrom(USER_LOCATION))) {
                    filteredPlanned.add(roadworks);
                }
            }
        }

        if (maxDistanceKm > 0) {
            filteredUnplanned.sort(Comparator.comparingDouble(e -> e.distanceKmFrom(USER_LOCATION)));
            filteredPlanned.sort(Comparator.comparingDouble(r -> r.distanceKmFrom(USER_LOCATION)));
        } else {
            filteredUnplanned.sort(Comparator.comparing(UnplannedRWData::getStartTime,
                    Comparator.nullsLast(Comparator.reverseOrder())));
            filteredPlanned.sort(Comparator.comparing(PlannedRWData::getStartTime,
                    Comparator.nullsLast(Comparator.reverseOrder())));
        }

        resultsChanged.setValue(true);
    }

    private boolean isWithinDistance(double km) {
        return maxDistanceKm == 0 || km <= maxDistanceKm;
    }

    public void selectPlanned(PlannedRWData roadworks) {
        selectedPlanned = roadworks;
        selectedUnplanned = null;
    }

    public void selectUnplanned(UnplannedRWData event) {
        selectedUnplanned = event;
        selectedPlanned = null;
    }

    public PlannedRWData getSelectedPlanned() { return selectedPlanned; }
    public UnplannedRWData getSelectedUnplanned() { return selectedUnplanned; }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
        mainThread.removeCallbacksAndMessages(null);
    }
}
