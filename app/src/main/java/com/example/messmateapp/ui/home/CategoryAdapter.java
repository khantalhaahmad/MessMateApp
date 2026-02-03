package com.example.messmateapp.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmateapp.R;
import com.example.messmateapp.domain.model.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    // 🔹 Click listener
    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    private OnCategoryClickListener listener;

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    private final List<Category> list = new ArrayList<>();

    // 🔥 SELECTED POSITION (default = All)
    private int selectedPosition = 0;

    // ✅ Required order
    private static final List<String> CATEGORY_ORDER = Arrays.asList(
            "all",
            "veg",
            "non-veg",
            "nonveg",
            "biryani",
            "chicken",
            "pizza",
            "fish"
    );

    public void submitList(List<Category> newList) {
        list.clear();

        if (newList != null) {
            newList.sort((c1, c2) ->
                    Integer.compare(
                            getOrderIndex(c1.getName()),
                            getOrderIndex(c2.getName())
                    )
            );
            list.addAll(newList);
        }

        selectedPosition = 0; // 🔥 default "All"
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        Category category = list.get(position);
        String name = category.getName();

        if (name == null) return;

        String key = name.toLowerCase(Locale.ROOT);

        // 🔹 ICON + TEXT
        switch (key) {
            case "all":
                holder.tvName.setText("All");
                holder.imgCategory.setImageResource(R.drawable.ic_all);
                break;

            case "veg":
                holder.tvName.setText("Veg");
                holder.imgCategory.setImageResource(R.drawable.ic_veg);
                break;

            case "non-veg":
            case "nonveg":
                holder.tvName.setText("Non-Veg");
                holder.imgCategory.setImageResource(R.drawable.ic_nonveg);
                break;

            case "biryani":
                holder.tvName.setText("Biryani");
                holder.imgCategory.setImageResource(R.drawable.ic_biryani);
                break;

            case "chicken":
                holder.tvName.setText("Chicken Thali");
                holder.imgCategory.setImageResource(R.drawable.ic_chicken);
                break;

            case "pizza":
            case "fish":
                holder.tvName.setText("Fish Thali");
                holder.imgCategory.setImageResource(R.drawable.ic_fish);
                break;
        }

        // 🔥 SELECTED UI (Underline + color)
        boolean isSelected = position == selectedPosition;

        holder.underline.setVisibility(
                isSelected ? View.VISIBLE : View.INVISIBLE
        );

        holder.tvName.setTextColor(
                holder.itemView.getContext().getColor(
                        isSelected ? R.color.orange : R.color.textPrimary
                )
        );

        // 🔥 CLICK
        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = position;

            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onCategoryClick(key);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private int getOrderIndex(String name) {
        if (name == null) return Integer.MAX_VALUE;
        int index = CATEGORY_ORDER.indexOf(name.toLowerCase(Locale.ROOT));
        return index == -1 ? Integer.MAX_VALUE : index;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCategory;
        TextView tvName;
        View underline;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            underline = itemView.findViewById(R.id.viewUnderline);
        }
    }
}
