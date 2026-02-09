package com.example.messmateapp.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
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
import com.example.messmateapp.ui.home.HomeActivity;
import android.widget.FrameLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    // Views
    private RecyclerView rvOrders;
    private OrderHistoryAdapter adapter;

    private ProgressBar progressBar;
    private TextView tvEmpty;

    // Bottom Nav
    private LinearLayout tabDelivery;
    private LinearLayout tabOrders;
    private FrameLayout layoutBottomNav;
    private View tabIndicator;


    private OrderRepositoryImpl repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        initViews();

        repository = new OrderRepositoryImpl(this);

        setupRecycler();

        setupBottomNav();     // Bottom navigation setup
        setOrdersSelected();  // Orders tab active

        loadOrders();         // Load API data
    }


    // ================= INIT =================

    private void initViews() {

        rvOrders = findViewById(R.id.rvOrders);

        progressBar = findViewById(R.id.progressBar);

        tvEmpty = findViewById(R.id.tvEmpty);

        // Bottom Nav
        layoutBottomNav = findViewById(R.id.layoutBottomNav);
        tabIndicator   = findViewById(R.id.tabIndicator);

        tabDelivery = findViewById(R.id.tabDelivery);
        tabOrders   = findViewById(R.id.tabOrders);
    }

    private void setOrdersSelected() {

        layoutBottomNav.post(() -> {

            int width = layoutBottomNav.getWidth() / 2;

            // half width
            tabIndicator.getLayoutParams().width = width;

            // move to Orders (right side)
            tabIndicator.animate().x(width).setDuration(200).start();

            tabIndicator.requestLayout();
        });
    }




    // ================= BOTTOM NAV =================

    private void setupBottomNav() {

        // Already on Orders → no action needed
        tabOrders.setOnClickListener(v -> {
            // Do nothing (Already here)
        });


        // Go to Home (Delivery)
        tabDelivery.setOnClickListener(v -> {

            Intent intent =
                    new Intent(OrderHistoryActivity.this,
                            HomeActivity.class);

            startActivity(intent);

            finish(); // close current page
        });
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
