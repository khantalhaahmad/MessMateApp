package com.example.messmateapp.ui.cart;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.messmateapp.R;
import com.example.messmateapp.data.model.RecommendationResponse;
import com.example.messmateapp.domain.model.CartItem;
import android.content.Context;
import android.util.Log;

import java.util.List;

public class RecommendationAdapter
        extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private final List<RecommendationResponse.RecommendationItem> list;
    private final Runnable onCartChanged;
    private final android.content.Context context;

    private String messId = "";
    private String messName = "";

    public RecommendationAdapter(
            Context ctx,
            List<RecommendationResponse.RecommendationItem> list,
            String messId,
            String messName,
            Runnable onCartChanged
    ) {
        this.context = ctx;
        this.list = list;

        // ✅ Use passed values if available
        if (messId != null && !messId.isEmpty()) {
            this.messId = messId;
        }

        if (messName != null && !messName.isEmpty()) {
            this.messName = messName;
        }

        this.onCartChanged = onCartChanged;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation, parent, false);

        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder h,
            int position
    ) {

        RecommendationResponse.RecommendationItem item =
                list.get(position);

// ✅ Fallback from API if not set
        if ((messId == null || messId.isEmpty()) && item.mess_id != null) {
            messId = item.mess_id;
        }

        if ((messName == null || messName.isEmpty()) && item.mess_name != null) {
            messName = item.mess_name;
        }

        /* ================= DATA ================= */

        h.name.setText(item.name);
        h.price.setText("₹" + item.price);

        Glide.with(h.image.getContext())
                .load(item.image)
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .into(h.image);


        /* ================= CART QTY ================= */

        int qty = CartManager.getItemQty(item.name);

        if (qty > 0) {

            h.btnAdd.setVisibility(View.GONE);
            h.layoutQty.setVisibility(View.VISIBLE);

            h.tvQty.setText(String.valueOf(qty));

        } else {

            h.btnAdd.setVisibility(View.VISIBLE);
            h.layoutQty.setVisibility(View.GONE);
        }


        /* ================= ADD (FLY ANIM) ================= */

        h.btnAdd.setOnClickListener(v -> {

            // ✅ SAVE RESTAURANT BEFORE ADDING
            Log.d("CART_DEBUG", "BANNER = " + item.messImage);

            CartManager.setRestaurant(
                    messId,
                    messName,
                    item.image,   // ✅ USE THIS
                    context
            );



            Log.d("CART_DEBUG",
                    "SET -> id=" + messId + " name=" + messName);

            // 🔥 THEN add item
            boolean added = CartManager.addItem(
                    new CartItem(
                            item.mess_id,          // id (ya item.id agar ho)
                            item.name,             // name

                            item.price,            // price (old)
                            item.price,            // latestPrice (same for now)

                            1,                     // quantity

                            item.image,            // image
                            item.type,             // type
                            item.category,         // category

                            messId,                // restaurantId
                            messName,              // restaurantName

                            true,                  // available
                            false                  // priceUpdated
                    ),
                    context
            );

            if (!added) {

                Toast.makeText(
                        v.getContext(),
                        "Max limit reached",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // 🔥 Fly animation
            startFlyAnimation(h.image, () -> {

                if (onCartChanged != null) {
                    onCartChanged.run();
                }
            });
        });

        /* ================= PLUS ================= */

        h.btnPlus.setOnClickListener(v -> {

            boolean inc = CartManager.increase(item.name, context);


            if (!inc) return;

            int pos = h.getBindingAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {
                notifyItemChanged(pos);
            }

            if (onCartChanged != null) {
                onCartChanged.run();
            }
        });


        /* ================= MINUS ================= */

        h.btnMinus.setOnClickListener(v -> {

            boolean inc = CartManager.increase(item.name, context);


            int pos = h.getBindingAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {
                notifyItemChanged(pos);
            }

            if (onCartChanged != null) {
                onCartChanged.run();
            }
        });
    }


    /* ================= FLY ANIMATION ================= */

    private void startFlyAnimation(ImageView image, Runnable onEnd) {

        if (image == null) {
            if (onEnd != null) onEnd.run();
            return;
        }

        View root = image.getRootView();


        Rect start = new Rect();
        Rect end = new Rect();


        image.getGlobalVisibleRect(start);

        // Cart bottom-right target
        root.getGlobalVisibleRect(end);


        float dx = end.right - start.centerX();
        float dy = end.bottom - start.centerY();


        image.bringToFront();


        ViewPropertyAnimator animator =
                image.animate()
                        .translationXBy(dx)
                        .translationYBy(dy)
                        .scaleX(0.2f)
                        .scaleY(0.2f)
                        .alpha(0f)
                        .setDuration(500);


        animator.withEndAction(() -> {

            // Reset
            image.setTranslationX(0f);
            image.setTranslationY(0f);
            image.setScaleX(1f);
            image.setScaleY(1f);
            image.setAlpha(1f);


            if (onEnd != null) {
                onEnd.run();
            }
        });

        animator.start();
    }


    @Override
    public int getItemCount() {
        return list.size();
    }


    /* ================= HOLDER ================= */

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        ImageView image;

        TextView name, price, tvQty;

        LinearLayout btnAdd;

        LinearLayout layoutQty;

        TextView btnPlus, btnMinus;


        ViewHolder(@NonNull View v) {

            super(v);

            image = v.findViewById(R.id.imgFood);

            name = v.findViewById(R.id.tvName);
            price = v.findViewById(R.id.tvPrice);

            btnAdd = v.findViewById(R.id.btnAdd);

            layoutQty = v.findViewById(R.id.layoutQty);

            tvQty = v.findViewById(R.id.tvQty);

            btnPlus = v.findViewById(R.id.btnPlus);
            btnMinus = v.findViewById(R.id.btnMinus);
        }
    }
}
