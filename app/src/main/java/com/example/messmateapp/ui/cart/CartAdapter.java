package com.example.messmateapp.ui.cart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmateapp.R;
import com.example.messmateapp.domain.model.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final Context context;   // ✅ NEW
    private final List<CartItem> list;
    private final Runnable onUpdate;


    // ✅ UPDATED CONSTRUCTOR
    public CartAdapter(Context context,
                       List<CartItem> list,
                       Runnable onUpdate) {

        this.context = context;
        this.list = list;
        this.onUpdate = onUpdate;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);

        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder h,
                                 int position) {

        if (position == RecyclerView.NO_POSITION) return;

        CartItem item = list.get(position);


        /* ================= DATA ================= */

        // Name
        h.tvName.setText(item.getName());

        // Price = price × qty
        int totalPrice =
                item.getPrice() * item.getQuantity();

        h.tvPrice.setText("₹" + totalPrice);

        // Qty
        h.tvQty.setText(
                String.valueOf(item.getQuantity())
        );


        /* ================= PLUS ================= */

        h.btnPlus.setOnClickListener(v -> {

            boolean inc =
                    CartManager.increase(
                            item.getId(),
                            context   // ✅ PASS CONTEXT
                    );

            if (!inc) return;


            int pos = h.getBindingAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {
                notifyItemChanged(pos);
            }


            if (onUpdate != null) {
                onUpdate.run();
            }
        });


        /* ================= MINUS ================= */

        h.btnMinus.setOnClickListener(v -> {

            boolean removed =
                    CartManager.decrease(
                            item.getId(),
                            context   // ✅ PASS CONTEXT
                    );


            int pos = h.getBindingAdapterPosition();

            if (pos == RecyclerView.NO_POSITION) return;


            // If item removed completely
            if (removed) {

                list.remove(pos);

                notifyItemRemoved(pos);
                notifyItemRangeChanged(
                        pos,
                        list.size()
                );

            } else {

                notifyItemChanged(pos);
            }


            if (onUpdate != null) {
                onUpdate.run();
            }
        });
    }


    @Override
    public int getItemCount() {

        return list.size();
    }


    /* ================= VIEW HOLDER ================= */

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvName, tvPrice, tvQty;

        ImageButton btnPlus, btnMinus;


        ViewHolder(@NonNull View v) {

            super(v);

            tvName = v.findViewById(R.id.tvItemName);
            tvPrice = v.findViewById(R.id.tvItemPrice);
            tvQty = v.findViewById(R.id.tvItemQty);

            btnPlus = v.findViewById(R.id.btnPlus);
            btnMinus = v.findViewById(R.id.btnMinus);
        }
    }
}
