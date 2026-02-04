package com.example.messmateapp.ui.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.messmateapp.R;
import com.example.messmateapp.domain.model.Restaurant;
import com.example.messmateapp.ui.menu.MenuActivity;

import java.util.ArrayList;
import java.util.List;

public class RestaurantAdapter
        extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    private final List<Restaurant> list = new ArrayList<>();
    private Context context;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        Restaurant restaurant = list.get(position);

        holder.tvName.setText(restaurant.getName());
        holder.tvLocation.setText(restaurant.getLocation());
        holder.tvRating.setText("⭐ " + restaurant.getRating());

        Glide.with(context)
                .load(restaurant.getBanner())
                .placeholder(R.drawable.placeholder)
                .circleCrop()   // 🔥 YAHI ADD
                .into(holder.imgRestaurant);


        // ✅ FIXED CLICK → OPEN MENU
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, MenuActivity.class);

            intent.putExtra("RESTAURANT_ID", restaurant.getId());
            intent.putExtra("RESTAURANT_NAME", restaurant.getName());

            // 🔥 PASS BANNER IMAGE
            intent.putExtra("RESTAURANT_IMAGE", restaurant.getBanner());

            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void submitList(List<Restaurant> newList) {
        list.clear();
        if (newList != null) {
            list.addAll(newList);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgRestaurant;
        TextView tvName, tvLocation, tvRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRestaurant = itemView.findViewById(R.id.imgRestaurant);
            tvName = itemView.findViewById(R.id.tvName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }
}
