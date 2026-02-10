package com.example.messmateapp.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmateapp.R;
import com.example.messmateapp.data.model.OrderDto;
import com.example.messmateapp.data.model.OrderHistoryResponse;
import com.example.messmateapp.data.repository.OrderRepositoryImpl;
import com.example.messmateapp.domain.model.CartItem;
import com.example.messmateapp.ui.cart.CartManager;
import com.example.messmateapp.ui.cart.CheckoutActivity;
import com.example.messmateapp.ui.home.HomeActivity;

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

    // Search
    private EditText etSearch;

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
        setupSearch();
        setupBottomNav();
        setOrdersSelected();

        loadOrders();
    }


    // ================= INIT =================

    private void initViews() {

        rvOrders = findViewById(R.id.rvOrders);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        etSearch = findViewById(R.id.etSearch);

        layoutBottomNav = findViewById(R.id.layoutBottomNav);
        tabIndicator = findViewById(R.id.tabIndicator);

        tabDelivery = findViewById(R.id.tabDelivery);
        tabOrders = findViewById(R.id.tabOrders);
    }


    // ================= SEARCH =================

    private void setupSearch() {

        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

                if (adapter == null) return;

                adapter.filter(s.toString());

                if (adapter.getItemCount() == 0) {

                    tvEmpty.setText("No matching orders 😕");
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvOrders.setVisibility(View.GONE);

                } else {

                    tvEmpty.setVisibility(View.GONE);
                    rvOrders.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    // ================= TAB INDICATOR =================

    private void setOrdersSelected() {

        layoutBottomNav.post(() -> {

            int width = layoutBottomNav.getWidth() / 2;

            tabIndicator.getLayoutParams().width = width;

            tabIndicator.animate()
                    .x(width)
                    .setDuration(200)
                    .start();

            tabIndicator.requestLayout();
        });
    }


    // ================= BOTTOM NAV =================

    private void setupBottomNav() {

        tabOrders.setOnClickListener(v -> {
            // Already here
        });

        tabDelivery.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            OrderHistoryActivity.this,
                            HomeActivity.class);

            startActivity(intent);
            finish();
        });
    }


    // ================= RECYCLER =================

    private void setupRecycler() {

        adapter = new OrderHistoryAdapter();

        // 🔥 Reorder Listener
        adapter.setOnReorderClickListener(order -> {

            handleReorder(order);

        });

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


    // ================= REORDER =================

    private void handleReorder(OrderDto order) {

        repository.reorderOrder(order.getId())
                .observe(this, res -> {

                    if (!res.isSuccess() || res.getData() == null) {

                        Toast.makeText(
                                this,
                                "Failed to reorder",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    List<CartItem> items = res.getData();

                    if (items.isEmpty()) {

                        Toast.makeText(
                                this,
                                "No items available",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    // ✅ Restaurant info from first item
                    CartItem first = items.get(0);

                    String resId = first.getRestaurantId();
                    String resName = first.getRestaurantName();
                    String resImage = ""; // later if available

                    CartManager.setCartForRestaurant(
                            resId,
                            items,
                            resName,
                            resImage,
                            this
                    );

// ✅ Open checkout with extras
                    Intent i = new Intent(this, CheckoutActivity.class);

                    i.putExtra("RESTAURANT_ID", resId);
                    i.putExtra("RESTAURANT_NAME", resName);
                    i.putExtra("RESTAURANT_IMAGE", resImage);

                    startActivity(i);

                });
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
