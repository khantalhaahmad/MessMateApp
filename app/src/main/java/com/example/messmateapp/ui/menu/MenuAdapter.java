package com.example.messmateapp.ui.menu;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.messmateapp.R;
import com.example.messmateapp.data.model.MenuItemDto;
import com.example.messmateapp.domain.model.CartItem;
import com.example.messmateapp.ui.cart.CartManager;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.content.Context;


public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    /* ==========================
       CALLBACK
       ========================== */

    public interface OnCartChanged {
        void onCartUpdated();
    }

    private final OnCartChanged cartChanged;


    private final Context context;

    public MenuAdapter(
            Context context,
            String messId,
            String messName,
            OnCartChanged listener
    ) {

        this.context = context;
        this.messId = messId;
        this.messName = messName;
        this.cartChanged = listener;
    }




    /* ==========================
       DATA LIST
       ========================== */

    // Current visible list
    private final List<MenuItemDto> list = new ArrayList<>();

    // Full backup list
    private final List<MenuItemDto> fullList = new ArrayList<>();

    private String messId;
    private String messName;

    /* ==========================
       SUBMIT LIST
       ========================== */

    public void submitList(List<MenuItemDto> items) {

        list.clear();
        fullList.clear();

        if (items != null) {

            list.addAll(items);
            fullList.addAll(items);
        }

        notifyDataSetChanged();
    }


    /* ==========================
       CHECK EMPTY (🔥 IMPORTANT)
       ========================== */

    public boolean isEmpty() {
        return list.isEmpty();
    }


    /* ==========================
       SYNC WITH CART
       ========================== */

    public void syncWithCart() {

        // Refresh all items from CartManager state
        notifyItemRangeChanged(0, getItemCount());
    }



    /* ==========================
       RESET AFTER ORDER
       ========================== */

    public void resetAllQuantities() {

        // Force refresh all visible items
        notifyItemRangeChanged(0, getItemCount());
    }



    /* ==========================
       SEARCH FILTER
       ========================== */

    public void filterByName(String query) {

        list.clear();

        if (query == null || query.trim().isEmpty()) {

            list.addAll(fullList);

        } else {

            String q = query.toLowerCase(Locale.ROOT);

            for (MenuItemDto item : fullList) {

                if (item.getName() != null &&
                        item.getName()
                                .toLowerCase(Locale.ROOT)
                                .contains(q)) {

                    list.add(item);
                }
            }
        }

        notifyDataSetChanged();
    }


    /* ==========================
       VEG FILTER
       ========================== */

    public void filterVeg(boolean veg) {

        list.clear();

        for (MenuItemDto item : fullList) {

            if (item.isVeg() == veg) {

                list.add(item);
            }
        }

        notifyDataSetChanged();
    }


    /* ==========================
       SHOW ALL
       ========================== */

    public void showAll() {

        list.clear();
        list.addAll(fullList);

        notifyDataSetChanged();
    }


    /* ==========================
       VIEW HOLDER
       ========================== */

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu, parent, false);

        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder h,
                                 int position) {

        MenuItemDto item = list.get(position);


        /* ==========================
           DATA
           ========================== */

        h.name.setText(item.getName());
        h.desc.setText(item.getDescription());
        h.price.setText("₹" + item.getPrice());


        Glide.with(h.image.getContext())
                .load(item.getImage())
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .into(h.image);


        /* ==========================
           CART QTY
           ========================== */

        int qty = CartManager.getItemQty(item.getId());


        /* ==========================
           UI STATE
           ========================== */

        if (qty > 0) {

            h.btnAdd.setVisibility(View.GONE);
            h.layoutQty.setVisibility(View.VISIBLE);

            h.tvQty.setText(String.valueOf(qty));

        } else {

            h.btnAdd.setVisibility(View.VISIBLE);
            h.layoutQty.setVisibility(View.GONE);
        }


        /* ==========================
           MAX LIMIT
           ========================== */

        boolean isMax = qty >= CartManager.MAX_QTY_PER_ITEM;

        h.btnPlus.setEnabled(!isMax);
        h.btnPlus.setAlpha(isMax ? 0.35f : 1f);


        /* ==========================
           CLEAR OLD LISTENERS
           ========================== */

        h.btnAdd.setOnClickListener(null);
        h.btnPlus.setOnClickListener(null);
        h.btnMinus.setOnClickListener(null);


        /* ==========================
           ADD
           ========================== */

        h.btnAdd.setOnClickListener(v -> {

            boolean added = CartManager.addItem(

                    new CartItem(
                            item.getId(),
                            item.getName(),
                            (int) item.getPrice(),
                            1,
                            item.getImage(),
                            item.isVeg() ? "veg" : "non-veg",
                            "other",
                            messId,     // ✅ Restaurant ID
                            messName    // ✅ Restaurant Name
                    ),

                    context   // ✅ Context yahin hona chahiye
            );




            if (!added) {

                showLimitSnackbar(v);
                return;
            }

            notifyItemChanged(h.getBindingAdapterPosition());
            cartChanged.onCartUpdated();
        });


        /* ==========================
           PLUS
           ========================== */

        h.btnPlus.setOnClickListener(v -> {

            int currentQty =
                    CartManager.getItemQty(item.getId());

            if (currentQty >= CartManager.MAX_QTY_PER_ITEM) {

                showLimitSnackbar(v);
                return;
            }

            CartManager.increase(item.getId(), context);



            notifyItemChanged(h.getBindingAdapterPosition());
            cartChanged.onCartUpdated();
        });


        /* ==========================
           MINUS
           ========================== */

        h.btnMinus.setOnClickListener(v -> {

            CartManager.decrease(item.getId(), context);



            notifyItemChanged(h.getBindingAdapterPosition());
            cartChanged.onCartUpdated();
        });
    }


    /* ==========================
       SNACKBAR
       ========================== */

    private void showLimitSnackbar(View anchor) {

        Snackbar snackbar = Snackbar.make(
                anchor,
                "You can add only "
                        + CartManager.MAX_QTY_PER_ITEM
                        + " of this item",
                Snackbar.LENGTH_SHORT
        );

        View sbView = snackbar.getView();

        sbView.setBackgroundResource(
                R.drawable.bg_snackbar_rounded
        );

        TextView text =
                sbView.findViewById(
                        com.google.android.material.R.id.snackbar_text
                );

        text.setTextColor(Color.WHITE);
        text.setTextSize(14f);
        text.setMaxLines(2);

        snackbar.show();
    }


    /* ==========================
       COUNT
       ========================== */

    @Override
    public int getItemCount() {
        return list.size();
    }


    /* ==========================
       HOLDER
       ========================== */

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;

        TextView name, desc, price, tvQty;

        Button btnAdd;

        LinearLayout layoutQty;

        TextView btnPlus, btnMinus;


        ViewHolder(@NonNull View v) {

            super(v);

            image = v.findViewById(R.id.imgMenu);

            name = v.findViewById(R.id.tvMenuName);
            desc = v.findViewById(R.id.tvMenuDesc);
            price = v.findViewById(R.id.tvMenuPrice);

            btnAdd = v.findViewById(R.id.btnAdd);

            layoutQty = v.findViewById(R.id.layoutQty);

            tvQty = v.findViewById(R.id.tvQty);

            btnPlus = v.findViewById(R.id.btnPlus);
            btnMinus = v.findViewById(R.id.btnMinus);
        }
    }
}
