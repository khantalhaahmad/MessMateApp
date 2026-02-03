package com.example.messmateapp.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmateapp.R;

public class ShimmerRestaurantAdapter
        extends RecyclerView.Adapter<ShimmerRestaurantAdapter.ViewHolder> {

    // 🔥 Number of shimmer cards
    private static final int SHIMMER_COUNT = 6;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant_shimmer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        // ❌ No binding needed for shimmer
    }

    @Override
    public int getItemCount() {
        return SHIMMER_COUNT;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
