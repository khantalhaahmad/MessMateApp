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

import com.bumptech.glide.Glide; // ✅ ADD
import com.example.messmateapp.R;
import com.example.messmateapp.ui.cart.CartManager;
import com.example.messmateapp.ui.menu.MenuActivity;
import android.util.Log;

import java.util.List;

public class AllCartsAdapter
        extends RecyclerView.Adapter<AllCartsAdapter.VH> {

    private final Context context;
    private final List<String> restaurantIds;

    public AllCartsAdapter(Context context,
                           List<String> restaurantIds) {

        this.context = context;
        this.restaurantIds = restaurantIds;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_all_cart, parent, false);

        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {

        String resId = restaurantIds.get(pos);

        int count = CartManager.getTotalItemsFor(resId);

        // ✅ Get real restaurant name
        String resName = CartManager.getRestaurantName(resId);

        // ✅ Get restaurant banner (ONLY ONCE)
        final String resImage =
                CartManager.getRestaurantImage(resId);

        Log.d("CART_DEBUG",
                "GET -> id=" + resId +
                        " name=" + resName +
                        " img=" + resImage);

        h.tvName.setText(resName);

        h.tvCount.setText(count + " item(s)");

        // ✅ Load banner image
        Glide.with(context)
                .load(resImage)
                .placeholder(R.drawable.placeholder) // loading time
                .error(R.drawable.placeholder)       // error time
                .circleCrop()
                .into(h.imgRestaurant);



        h.btnView.setOnClickListener(v -> {

            Intent i = new Intent(context, MenuActivity.class);

            i.putExtra("RESTAURANT_ID", resId);
            i.putExtra("RESTAURANT_IMAGE", resImage); // ✅ reuse

            context.startActivity(i);
        });

        h.btnClose.setOnClickListener(v -> {

            CartManager.clearByRestaurant(resId, context);

            restaurantIds.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, restaurantIds.size());
        });
    }


    @Override
    public int getItemCount() {
        return restaurantIds.size();
    }


    static class VH extends RecyclerView.ViewHolder {

        TextView tvName, tvCount, btnView;
        ImageView btnClose;

        // ✅ ADD restaurant image
        ImageView imgRestaurant;

        VH(View v) {
            super(v);

            tvName = v.findViewById(R.id.tvResName);
            tvCount = v.findViewById(R.id.tvItemCount);
            btnView = v.findViewById(R.id.btnViewCart);
            btnClose = v.findViewById(R.id.btnClose);

            // ✅ Bind image view
            imgRestaurant = v.findViewById(R.id.imgRestaurant);
        }
    }
}
