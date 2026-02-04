package com.example.messmateapp.ui.menu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmateapp.R;
import com.example.messmateapp.data.model.MenuResponse;
import com.example.messmateapp.data.remote.ApiClient;
import com.example.messmateapp.data.remote.ApiService;
import com.example.messmateapp.ui.cart.CartManager;
import com.example.messmateapp.ui.cart.CheckoutActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuActivity extends AppCompatActivity {

    /* ================= CONST ================= */

    private static final int CHECKOUT_REQUEST = 101;


    /* ================= DATA ================= */

    private MenuAdapter adapter;
    private ApiService api;

    private String messId = "";
    private String messName = "";


    /* ================= UI ================= */

    private View cartBar;
    private TextView tvCartSummary;

    private RecyclerView rvMenu;
    private View layoutNoResult;

    private Button btnAll, btnVeg, btnNonVeg;
    private TextView tvRating;


    // 🔥 Search Animation Views
    private EditText etSearch;
    private TextSwitcher tsSearchHint;
    private TextView tvSearchPrefix;

    private final Handler hintHandler = new Handler(Looper.getMainLooper());
    private Runnable hintRunnable;
    private int hintIndex = 0;
    private String messImage = "";

    private final String[] menuHints = {
            "\"Chicken Biryani\" 🍗",
            "\"Veg Biryani\" 🥗",
            "\"Fish Curry\" 🐟",
            "\"Paneer\" 🧀",
            "\"Fried Rice\" 🍚",
            "\"Butter Chicken\" 🍛"
    };


    /* ================= LifeCycle ================= */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);

        initIntent();

// 🔥 IMPORTANT: Set current restaurant cart (with context)
        CartManager.setRestaurant(
                messId,
                messName,
                messImage,   // ✅ banner
                this
        );




        initViews();


        api = ApiClient.getClient().create(ApiService.class);

        fetchMenu();
        fetchRating();


        updateCartBar();

        setupSearchHint();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensure correct restaurant is active
        CartManager.setRestaurant(
                messId,
                messName,
                messImage,   // ✅ ADD IMAGE
                this
        );



        // Sync cart after checkout
        CartManager.sync(this);

        // Refresh adapter
        if (adapter != null) {
            adapter.resetAllQuantities();
        }

        // Refresh cart bar
        updateCartBar();
    }



    /* ================= Init Intent ================= */

    private void initIntent() {




        if (getIntent() != null) {

            messId = getIntent().getStringExtra("RESTAURANT_ID");
            messName = getIntent().getStringExtra("RESTAURANT_NAME");
            messImage = getIntent().getStringExtra("RESTAURANT_IMAGE");

            if (messId == null) messId = "";
            if (messName == null) messName = "";
        }

        if (messId.isEmpty()) {

            Toast.makeText(this, "Invalid restaurant", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    /* ================= Init Views ================= */

    private void initViews() {

        /* ================= Header ================= */

        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvTitle = findViewById(R.id.tvRestaurantName);

        TextView tvDistance = findViewById(R.id.tvDistance);
        TextView tvTime = findViewById(R.id.tvTime);
        tvRating = findViewById(R.id.tvRating);


        etSearch = findViewById(R.id.etSearchTop);
        tsSearchHint = findViewById(R.id.tsMenuSearchHint);
        tvSearchPrefix = findViewById(R.id.tvMenuSearchPrefix);


        /* ================= Main ================= */

        rvMenu = findViewById(R.id.rvMenu);
        layoutNoResult = findViewById(R.id.layoutNoResult);

        btnAll = findViewById(R.id.btnAll);
        btnVeg = findViewById(R.id.btnVeg);
        btnNonVeg = findViewById(R.id.btnNonVeg);

        cartBar = findViewById(R.id.cartBar);
        tvCartSummary = findViewById(R.id.tvCartSummary);

        TextView btnViewCart = findViewById(R.id.btnViewCart);


        /* ================= Header Data ================= */

        tvTitle.setText(messName);

        tvDistance.setText("9.5 km");
        tvTime.setText("20-30 min");

        btnBack.setOnClickListener(v -> onBackPressed());


        /* ================= Adapter ================= */

        adapter = new MenuAdapter(
                this,
                messId,
                messName,
                this::updateCartBar
        );



        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        rvMenu.setAdapter(adapter);


        /* ================= Filters ================= */

        selectFilter(btnAll, btnAll, btnVeg, btnNonVeg);


        btnAll.setOnClickListener(v -> {

            adapter.showAll();
            checkEmpty();

            selectFilter(btnAll, btnAll, btnVeg, btnNonVeg);
        });


        btnVeg.setOnClickListener(v -> {

            adapter.filterVeg(true);
            checkEmpty();

            selectFilter(btnVeg, btnAll, btnVeg, btnNonVeg);
        });


        btnNonVeg.setOnClickListener(v -> {

            adapter.filterVeg(false);
            checkEmpty();

            selectFilter(btnNonVeg, btnAll, btnVeg, btnNonVeg);
        });


        /* ================= Search ================= */

        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().isEmpty()) {

                    tsSearchHint.setVisibility(View.VISIBLE);
                    tvSearchPrefix.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Hide hint while typing
                tsSearchHint.setVisibility(View.GONE);
                tvSearchPrefix.setVisibility(View.GONE);

                adapter.filterByName(s.toString());
                checkEmpty();
            }
        });


        /* ================= View Cart ================= */

        btnViewCart.setOnClickListener(v -> openCart());
    }


    /* ================= Search Hint Setup ================= */

    private void setupSearchHint() {

        tsSearchHint.setFactory(() -> {

            TextView tv = new TextView(this);

            tv.setTextSize(15);
            tv.setTextColor(getResources().getColor(R.color.textSecondary));
            tv.setGravity(Gravity.CENTER_VERTICAL);

            return tv;
        });

        tsSearchHint.setInAnimation(
                AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom));

        tsSearchHint.setOutAnimation(
                AnimationUtils.loadAnimation(this, R.anim.slide_out_top));

        startHintLoop();
    }


    private void startHintLoop() {

        hintRunnable = new Runnable() {
            @Override
            public void run() {

                if (etSearch.getText().toString().isEmpty()) {

                    tsSearchHint.setText(menuHints[hintIndex]);

                    hintIndex++;

                    if (hintIndex >= menuHints.length) {
                        hintIndex = 0;
                    }

                    tsSearchHint.setVisibility(View.VISIBLE);
                    tvSearchPrefix.setVisibility(View.VISIBLE);
                }

                hintHandler.postDelayed(this, 2500);
            }
        };

        hintHandler.post(hintRunnable);
    }


    /* ================= Filter UI Helper ================= */

    private void selectFilter(Button selected,
                              Button btnAll,
                              Button btnVeg,
                              Button btnNonVeg) {

        btnAll.setBackgroundResource(R.drawable.bg_filter_unselected);
        btnVeg.setBackgroundResource(R.drawable.bg_filter_unselected);
        btnNonVeg.setBackgroundResource(R.drawable.bg_filter_unselected);

        btnAll.setTextColor(getResources().getColor(R.color.black));
        btnVeg.setTextColor(getResources().getColor(R.color.black));
        btnNonVeg.setTextColor(getResources().getColor(R.color.black));

        selected.setBackgroundResource(R.drawable.bg_filter_selected);
        selected.setTextColor(getResources().getColor(android.R.color.white));
    }


    /* ================= Check Empty ================= */

    private void checkEmpty() {

        if (adapter == null) return;

        if (adapter.getItemCount() == 0) {

            rvMenu.setVisibility(View.GONE);
            layoutNoResult.setVisibility(View.VISIBLE);

        } else {

            rvMenu.setVisibility(View.VISIBLE);
            layoutNoResult.setVisibility(View.GONE);
        }
    }


    /* ================= Open Cart ================= */

    private void openCart() {

        Intent intent =
                new Intent(MenuActivity.this, CheckoutActivity.class);

        intent.putExtra("RESTAURANT_ID", messId);
        intent.putExtra("RESTAURANT_NAME", messName);

        startActivityForResult(intent, CHECKOUT_REQUEST);
    }


    /* ================= Cart Bar ================= */

    private void updateCartBar() {

        if (cartBar == null) return;

        int count = CartManager.getTotalItems();

        if (count > 0) {

            cartBar.setVisibility(View.VISIBLE);
            tvCartSummary.setText(count + " items added");

        } else {

            cartBar.setVisibility(View.GONE);
            tvCartSummary.setText("");
        }
    }


    /* ================= Fetch Menu ================= */

    private void fetchMenu() {

        if (messId.isEmpty()) return;


        api.getMenu(messId).enqueue(new Callback<MenuResponse>() {

            @Override
            public void onResponse(Call<MenuResponse> call,
                                   Response<MenuResponse> response) {

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().success
                        && response.body().data != null) {

                    adapter.submitList(response.body().data);

                    adapter.syncWithCart();

                    checkEmpty();

                    updateCartBar();

                } else {

                    checkEmpty();

                    Toast.makeText(
                            MenuActivity.this,
                            "No menu available",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call,
                                  Throwable t) {

                Toast.makeText(
                        MenuActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    /* ================= Fetch Rating ================= */

    private void fetchRating() {

        api.getMessById(messId).enqueue(new Callback<com.example.messmateapp.data.model.RestaurantDto>() {

            @Override
            public void onResponse(Call<com.example.messmateapp.data.model.RestaurantDto> call,
                                   Response<com.example.messmateapp.data.model.RestaurantDto> response) {

                if (response.isSuccessful() && response.body() != null) {

                    double rating = response.body().getRating();

                    tvRating.setText(String.format("%.1f", rating));

                } else {

                    tvRating.setText("0.0");
                }
            }

            @Override
            public void onFailure(Call<com.example.messmateapp.data.model.RestaurantDto> call,
                                  Throwable t) {

                tvRating.setText("0.0");
            }
        });
    }

    /* ================= Cleanup ================= */

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (hintHandler != null && hintRunnable != null) {
            hintHandler.removeCallbacks(hintRunnable);
        }
    }
}
