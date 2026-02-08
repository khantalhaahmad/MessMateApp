package com.example.messmateapp.ui.order;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmateapp.R;
import com.example.messmateapp.data.model.OrderDto;
import com.example.messmateapp.data.model.OrderHistoryResponse;
import com.example.messmateapp.data.repository.OrderRepositoryImpl;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private OrderHistoryAdapter adapter;

    private ProgressBar progressBar;
    private TextView tvEmpty;

    private OrderRepositoryImpl repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        initViews();

        repository = new OrderRepositoryImpl(OrderHistoryActivity.this);



        setupRecycler();

        loadOrders();
    }

    // ================= INIT =================

    private void initViews() {

        rvOrders = findViewById(R.id.rvOrders);

        progressBar = findViewById(R.id.progressBar);

        tvEmpty = findViewById(R.id.tvEmpty);
    }

    // ================= RECYCLER =================

    private void setupRecycler() {

        adapter = new OrderHistoryAdapter();

        rvOrders.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvOrders.setAdapter(adapter);
    }

    // ================= API CALL =================

    private void loadOrders() {

        showLoading(true);

        repository.getMyOrders().enqueue(
                new Callback<OrderHistoryResponse>() {

                    @Override
                    public void onResponse(
                            Call<OrderHistoryResponse> call,
                            Response<OrderHistoryResponse> response) {

                        showLoading(false);

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            List<OrderDto> orders =
                                    response.body().getOrders();

                            if (orders == null || orders.isEmpty()) {

                                showEmpty();

                            } else {

                                adapter.setData(orders);

                                rvOrders.setVisibility(View.VISIBLE);
                                tvEmpty.setVisibility(View.GONE);
                            }

                        } else {

                            showError("Failed to load orders");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<OrderHistoryResponse> call,
                            Throwable t) {

                        showLoading(false);

                        showError("Network error: " + t.getMessage());
                    }
                }
        );
    }

    // ================= UI STATES =================

    private void showLoading(boolean show) {

        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showEmpty() {

        rvOrders.setVisibility(View.GONE);

        tvEmpty.setVisibility(View.VISIBLE);

        tvEmpty.setText("No orders found 😕");
    }

    private void showError(String msg) {

        Toast.makeText(
                this,
                msg,
                Toast.LENGTH_SHORT
        ).show();
    }
}
