package com.example.anwar_ahmed_s2534054.Situations;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anwar_ahmed_s2534054.DataObjects.PlannedRWData;
import com.example.anwar_ahmed_s2534054.DataObjects.UnplannedRWData;
import com.example.anwar_ahmed_s2534054.MapBuilder.SituationIcons;
import com.example.anwar_ahmed_s2534054.R;
import com.example.anwar_ahmed_s2534054.Utility.DateTimeUtils;
import com.example.anwar_ahmed_s2534054.Utility.Formatters;

import java.util.ArrayList;

public class SituationAdapter extends RecyclerView.Adapter<SituationAdapter.ViewHolder> {
    private final SituationNavigator navigator;
    private final ArrayList<UnplannedRWData> unplanned = new ArrayList<>();
    private final ArrayList<PlannedRWData> planned = new ArrayList<>();

    private Pair<Double, Double> userLocation;

    public SituationAdapter(SituationNavigator navigator) {
        this.navigator = navigator;
    }

    public void setItems(ArrayList<UnplannedRWData> unplanned, ArrayList<PlannedRWData> planned,
                         Pair<Double, Double> userLocation) {
        this.unplanned.clear();
        this.unplanned.addAll(unplanned);
        this.planned.clear();
        this.planned.addAll(planned);
        this.userLocation = userLocation;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return unplanned.size() + planned.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_situation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < unplanned.size()) {
            bindUnplanned(holder, unplanned.get(position));
        } else {
            bindPlanned(holder, planned.get(position - unplanned.size()));
        }
    }

    private void bindUnplanned(ViewHolder holder, UnplannedRWData event) {
        final Context context = holder.itemView.getContext();
        final SituationType type = event.getType();

        SituationIcons.applyTo(holder.icon, event.getIconRes());
        holder.typeText.setText(event.getTypeLabel());
        holder.typeText.setTextColor(ContextCompat.getColor(context, type.getColorRes()));
        holder.descriptionText.setText(Formatters.orNA(event.getDescription()));

        holder.durationBar.setVisibility(View.INVISIBLE);
        holder.durationPill.setVisibility(View.GONE);

        showDistance(holder, event.hasLocation(), event.distanceKmFrom(userLocation));

        holder.itemView.setContentDescription(event.getTypeLabel() + ". " + event.getDescription());
        holder.itemView.setOnClickListener(v -> navigator.showUnplannedDetail(event));
    }

    private void bindPlanned(ViewHolder holder, PlannedRWData roadworks) {
        final Context context = holder.itemView.getContext();
        final SituationType type = roadworks.getType();

        SituationIcons.applyTo(holder.icon, roadworks.getIconRes());
        holder.typeText.setText(roadworks.getTypeLabel());
        holder.typeText.setTextColor(ContextCompat.getColor(context, type.getColorRes()));
        holder.descriptionText.setText(Formatters.orNA(roadworks.getDescription()));

        final DurationCategory category = roadworks.getDurationCategory();
        final int durationColor = ContextCompat.getColor(context, category.getColorRes());
        holder.durationBar.setBackgroundColor(durationColor);
        holder.durationBar.setVisibility(View.VISIBLE);
        holder.durationPill.setText(category.getLabel() + " · "
                + DateTimeUtils.formatDuration(roadworks.getStartTime(), roadworks.getEndTime()));
        holder.durationPill.setBackgroundTintList(ColorStateList.valueOf(durationColor));
        holder.durationPill.setVisibility(View.VISIBLE);

        showDistance(holder, roadworks.hasLocation(), roadworks.distanceKmFrom(userLocation));

        holder.itemView.setContentDescription(roadworks.getTypeLabel() + ". " + roadworks.getDescription());
        holder.itemView.setOnClickListener(v -> navigator.showPlannedDetail(roadworks));
    }

    private void showDistance(ViewHolder holder, boolean hasLocation, double km) {
        if (hasLocation) {
            holder.distanceText.setText(Formatters.distance(km));
            holder.distanceText.setVisibility(View.VISIBLE);
        } else {
            holder.distanceText.setVisibility(View.GONE);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View durationBar;
        final ImageView icon;
        final TextView typeText;
        final TextView descriptionText;
        final TextView durationPill;
        final TextView distanceText;

        ViewHolder(View itemView) {
            super(itemView);
            durationBar = itemView.findViewById(R.id.durationBar);
            icon = itemView.findViewById(R.id.typeIcon);
            typeText = itemView.findViewById(R.id.typeText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            durationPill = itemView.findViewById(R.id.durationPill);
            distanceText = itemView.findViewById(R.id.distanceText);
        }
    }
}
