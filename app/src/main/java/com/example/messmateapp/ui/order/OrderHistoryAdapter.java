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

    private final List<OrderDto> list = new ArrayList<>();


    // ================= SET DATA =================

    public void setData(List<OrderDto> orders) {

        list.clear();

        if (orders != null) {
            list.addAll(orders);
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


        // 🏪 Mess Name
        holder.tvMess.setText(order.getMessName());


        // 🍽️ Items + Quantity
        StringBuilder itemsText = new StringBuilder();

        if (order.getItems() != null) {

            for (OrderDto.ItemDto item : order.getItems()) {

                itemsText.append(item.getQuantity())
                        .append(" x ")
                        .append(item.getName())
                        .append(", ");
            }

            // Remove last comma
            if (itemsText.length() > 2) {
                itemsText.setLength(itemsText.length() - 2);
            }
        }

        holder.tvItems.setText(itemsText.toString());


        // 💰 Price
        holder.tvPrice.setText("₹" + order.getTotalPrice());


        // 📦 Status
        holder.tvStatus.setText(order.getStatus());


        // 💳 Payment
        holder.tvPayment.setText(order.getPaymentMethod());


        // ⏰ Time
        holder.tvTime.setText(
                TimeUtils.getTimeAgo(order.getCreatedAt())
        );


        // 🖼️ Mess Image
        if (order.getMessImage() != null) {

            Glide.with(holder.itemView.getContext())
                    .load(order.getMessImage())
                    .placeholder(R.drawable.ic_food)
                    .into(holder.imgMess);
        }
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


        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            // Image
            imgMess = itemView.findViewById(R.id.imgMess);


            // Text
            tvMess   = itemView.findViewById(R.id.tvMessName);
            tvItems  = itemView.findViewById(R.id.tvOrderItems);
            tvTime   = itemView.findViewById(R.id.tvOrderTime);

            tvPrice  = itemView.findViewById(R.id.tvOrderPrice);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvPayment= itemView.findViewById(R.id.tvPaymentMethod);
        }
    }
}
