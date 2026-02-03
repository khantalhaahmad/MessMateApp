package com.example.messmateapp.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.messmateapp.R;
import com.example.messmateapp.domain.model.Banner;

import java.util.ArrayList;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private final List<Banner> banners = new ArrayList<>();

    // ================= SUBMIT LIST =================
    public void submitList(@NonNull List<Banner> newList) {
        DiffUtil.DiffResult diffResult =
                DiffUtil.calculateDiff(new BannerDiffCallback(this.banners, newList));
        this.banners.clear();
        this.banners.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    // ================= VIEW HOLDER =================
    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        holder.bind(banners.get(position));
    }

    @Override
    public int getItemCount() {
        return banners.size();
    }

    // ================= VIEW HOLDER CLASS =================
    static class BannerViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgBanner;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBanner = itemView.findViewById(R.id.imgBanner);
        }

        void bind(Banner banner) {
            Glide.with(imgBanner.getContext())
                    .load(banner.getImageUrl())   // ✅ FULL URL from backend
                    .placeholder(R.drawable.placeholder) // 🔥 add placeholder
                    .error(R.drawable.placeholder)       // 🔥 fallback
                    .centerCrop()
                    .into(imgBanner);
        }
    }

    // ================= DIFF UTIL =================
    static class BannerDiffCallback extends DiffUtil.Callback {

        private final List<Banner> oldList;
        private final List<Banner> newList;

        BannerDiffCallback(List<Banner> oldList, List<Banner> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId()
                    .equals(newList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition)
                    .equals(newList.get(newItemPosition));
        }
    }
}
