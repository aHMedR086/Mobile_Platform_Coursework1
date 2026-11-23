package com.example.anwar_ahmed_s2534054.Situations;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anwar_ahmed_s2534054.R;
import com.google.android.material.chip.ChipGroup;

public class SituationListFragment extends Fragment {
    private SituationsViewModel viewModel;
    private SituationAdapter adapter;
    private TextView emptyText;
    private TextView statusText;
    private View progress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_situation_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SituationsViewModel.class);

        RecyclerView situationList = view.findViewById(R.id.situationList);
        emptyText = view.findViewById(R.id.emptyText);
        statusText = view.findViewById(R.id.statusText);
        progress = view.findViewById(R.id.progress);

        adapter = new SituationAdapter((SituationNavigator) requireActivity());
        situationList.setLayoutManager(new LinearLayoutManager(requireContext()));
        situationList.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        situationList.setAdapter(adapter);

        setUpFilters(view);
        setUpLegend(view);

        viewModel.getResultsChanged().observe(getViewLifecycleOwner(), changed -> showSituations());
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            updateEmptyText();
            updateStatus();
        });
        viewModel.getError().observe(getViewLifecycleOwner(), message -> updateStatus());
    }

    private void setUpFilters(View view) {
        final ChipGroup chipGroup = view.findViewById(R.id.typeChipGroup);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            final int id = checkedIds.get(0);
            if (id == R.id.chipRoadworks) viewModel.setTypeFilter(SituationsViewModel.TypeFilter.ROADWORKS);
            else if (id == R.id.chipIncidents) viewModel.setTypeFilter(SituationsViewModel.TypeFilter.INCIDENTS);
            else viewModel.setTypeFilter(SituationsViewModel.TypeFilter.ALL);
        });

        final EditText roadInput = view.findViewById(R.id.roadSearchInput);
        roadInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                viewModel.setRoadQuery(s.toString());
            }
        });
        roadInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard(v);
                return true;
            }
            return false;
        });

        final Spinner distanceSpinner = view.findViewById(R.id.distanceSpinner);
        final ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.distance_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceSpinner.setAdapter(spinnerAdapter);
        distanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                viewModel.setMaxDistanceKm(SituationsViewModel.DISTANCES[position]);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setUpLegend(View view) {
        setLegendPill(view.findViewById(R.id.legendShort), DurationCategory.SHORT);
        setLegendPill(view.findViewById(R.id.legendMedium), DurationCategory.MEDIUM);
        setLegendPill(view.findViewById(R.id.legendLong), DurationCategory.LONG);
    }

    private void setLegendPill(TextView pill, DurationCategory category) {
        pill.setText(category.getRangeText());
        pill.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), category.getColorRes())));
    }

    private void showSituations() {
        adapter.setItems(viewModel.getFilteredUnplanned(), viewModel.getFilteredPlanned(),
                SituationsViewModel.USER_LOCATION);
        updateEmptyText();
        updateStatus();
    }

    private void updateEmptyText() {
        final boolean empty = adapter.getItemCount() == 0 && !Boolean.TRUE.equals(viewModel.getLoading().getValue());
        emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void updateStatus() {
        final int grey = ContextCompat.getColor(requireContext(), R.color.type_incident);
        if (Boolean.TRUE.equals(viewModel.getLoading().getValue())) {
            statusText.setText(R.string.loading);
            statusText.setTextColor(grey);
            return;
        }
        final String error = viewModel.getError().getValue();
        if (error != null) {
            statusText.setText(error);
            statusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.type_closure));
            return;
        }
        statusText.setText("Showing " + adapter.getItemCount() + " of " + viewModel.getTotalCount()
                + " situations");
        statusText.setTextColor(grey);
    }

    private void hideKeyboard(View view) {
        final InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
