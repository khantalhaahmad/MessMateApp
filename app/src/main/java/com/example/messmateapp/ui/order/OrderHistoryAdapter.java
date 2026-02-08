package com.example.messmateapp.ui.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmateapp.R;
import com.example.messmateapp.data.model.OrderDto;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryAdapter
        extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    private final List<OrderDto> list = new ArrayList<>();

    public void setData(List<OrderDto> orders) {

        list.clear();

        if (orders != null) {
            list.addAll(orders);
        }

        notifyDataSetChanged();
    }

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

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        OrderDto order = list.get(position);

        holder.tvMess.setText(order.getMessName());

        holder.tvPrice.setText(
                "₹" + order.getTotalPrice());

        holder.tvStatus.setText(order.getStatus());

        holder.tvPayment.setText(
                order.getPaymentMethod());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvMess, tvPrice, tvStatus, tvPayment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMess = itemView.findViewById(R.id.tvMessName);
            tvPrice = itemView.findViewById(R.id.tvOrderPrice);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvPayment = itemView.findViewById(R.id.tvPaymentMethod);
        }
    }
}
