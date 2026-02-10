package com.example.messmateapp.ui.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.messmateapp.R;
import com.example.messmateapp.data.model.OrderDto;
import com.example.messmateapp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryAdapter
        extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    // ================= LIST =================

    private final List<OrderDto> list = new ArrayList<>();
    private final List<OrderDto> originalList = new ArrayList<>();


    // ================= REORDER LISTENER =================

    public interface OnReorderClickListener {
        void onReorderClick(OrderDto order);
    }

    private OnReorderClickListener listener;

    public void setOnReorderClickListener(OnReorderClickListener listener) {
        this.listener = listener;
    }


    // ================= SET DATA =================

    public void setData(List<OrderDto> orders) {

        list.clear();
        originalList.clear();

        if (orders != null) {
            list.addAll(orders);
            originalList.addAll(orders);
        }

        notifyDataSetChanged();
    }


    // ================= SEARCH FILTER =================

    public void filter(String text) {

        list.clear();

        if (text == null || text.trim().isEmpty()) {

            list.addAll(originalList);

        } else {

            text = text.toLowerCase();

            for (OrderDto order : originalList) {

                boolean match = false;

                // Search in mess name
                if (order.getMessName() != null &&
                        order.getMessName()
                                .toLowerCase()
                                .contains(text)) {

                    match = true;
                }

                // Search in food items
                if (!match && order.getItems() != null) {

                    for (OrderDto.ItemDto item : order.getItems()) {

                        if (item.getName() != null &&
                                item.getName()
                                        .toLowerCase()
                                        .contains(text)) {

                            match = true;
                            break;
                        }
                    }
                }

                if (match) {
                    list.add(order);
                }
            }
        }

        notifyDataSetChanged();
    }


    // ================= CREATE VIEW =================

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history,
                        parent,
                        false);

        return new ViewHolder(v);
    }


    // ================= BIND DATA =================

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        OrderDto order = list.get(position);


        // Mess Name
        holder.tvMess.setText(order.getMessName());


        // Items + Quantity
        StringBuilder itemsText = new StringBuilder();

        if (order.getItems() != null) {

            for (OrderDto.ItemDto item : order.getItems()) {

                itemsText.append(item.getQuantity())
                        .append(" x ")
                        .append(item.getName())
                        .append(", ");
            }

            if (itemsText.length() > 2) {
                itemsText.setLength(itemsText.length() - 2);
            }
        }

        holder.tvItems.setText(itemsText.toString());


        // Price
        holder.tvPrice.setText("₹" + order.getTotalPrice());


        // Status
        holder.tvStatus.setText(order.getStatus());


        // Payment
        holder.tvPayment.setText(order.getPaymentMethod());


        // Time
        holder.tvTime.setText(
                TimeUtils.getTimeAgo(order.getCreatedAt())
        );


        // Image
        Glide.with(holder.itemView.getContext())
                .load(order.getMessImage())
                .placeholder(R.drawable.ic_food)
                .error(R.drawable.ic_food)
                .into(holder.imgMess);


        // ================= REORDER CLICK =================

        holder.btnReorder.setOnClickListener(v -> {

            if (listener != null) {
                listener.onReorderClick(order);
            }

        });
    }


    // ================= COUNT =================

    @Override
    public int getItemCount() {
        return list.size();
    }


    // ================= VIEW HOLDER =================

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgMess;

        TextView tvMess, tvItems, tvTime,
                tvPrice, tvStatus, tvPayment;

        TextView btnReorder; // 👈 Reorder Button


        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            // Image
            imgMess = itemView.findViewById(R.id.imgMess);


            // Text
            tvMess    = itemView.findViewById(R.id.tvMessName);
            tvItems   = itemView.findViewById(R.id.tvOrderItems);
            tvTime    = itemView.findViewById(R.id.tvOrderTime);

            tvPrice   = itemView.findViewById(R.id.tvOrderPrice);
            tvStatus  = itemView.findViewById(R.id.tvOrderStatus);
            tvPayment = itemView.findViewById(R.id.tvPaymentMethod);


            // Reorder Button
            btnReorder = itemView.findViewById(R.id.btnReorder);
        }
    }
}
