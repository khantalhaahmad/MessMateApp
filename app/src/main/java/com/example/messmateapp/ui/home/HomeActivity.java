package com.example.messmateapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmateapp.R;
import com.example.messmateapp.data.repository.HomeRepositoryImpl;
import com.example.messmateapp.domain.model.MenuItem;
import com.example.messmateapp.domain.model.Restaurant;
import com.example.messmateapp.domain.usecase.GetBannersUseCase;
import com.example.messmateapp.domain.usecase.GetCategoriesUseCase;
import com.example.messmateapp.domain.usecase.GetRestaurantsUseCase;
import com.example.messmateapp.ui.profile.ProfileActivity;
import com.example.messmateapp.utils.Resource;
import com.example.messmateapp.ui.cart.CartManager;
import com.google.android.material.snackbar.Snackbar;
import com.example.messmateapp.ui.cart.CheckoutActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private boolean isPopupShown = false;

    private RecyclerView rvCategories, rvRestaurants;
    private CategoryAdapter categoryAdapter;
    private RestaurantAdapter restaurantAdapter;
    private ShimmerRestaurantAdapter shimmerAdapter;
    private HomeViewModel viewModel;

    private EditText etSearch;
    private TextSwitcher tsSearchHint;
    private TextView tvSearchPrefix;

    private View emptyState;

    private ImageView imgProfile;

    private final List<Restaurant> allRestaurants = new ArrayList<>();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingRunnable;

    private TextView btnAllCarts;


    // ================= ZOMATO STYLE FOOD NAMES =================

    private final String[] zomatoHints = {
            "\"Biryani\" 🍗",
            "\"Momos\" 🥟",
            "\"Fried Rice\" 🍚",
            "\"Burger\" 🍔",
            "\"Pizza\" 🍕",
            "\"Rolls\" 🌯"
    };


    private int hintIndex = 0;

    private final Handler hintHandler = new Handler(Looper.getMainLooper());
    private Runnable hintRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupRecyclerViews();
        setupViewModel();
        observeData();
        setupSearch();
        setupProfileClick();
        setupSearchHintSwitcher();

        updateAllCartsButton(); // ✅ ADD THIS
    }


    @Override
    protected void onResume() {
        super.onResume();

        CartManager.loadFromStorage(this);

        updateAllCartsButton();

        bringAllCartsToFront(); // ✅ ADD THIS

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showAllCartPopups();
        }, 200);
    }

    private void updateAllCartsButton() {

        Log.d("CART_DEBUG",
                "Active carts = " + CartManager.getActiveCartCount());

        int totalCarts = CartManager.getActiveCartCount();

        if (totalCarts > 1) {
            btnAllCarts.setVisibility(View.VISIBLE);
            btnAllCarts.setText("All Carts (" + totalCarts + ")");
        } else {
            btnAllCarts.setVisibility(View.GONE);
        }
    }


    private void bringAllCartsToFront() {
        if (btnAllCarts != null) {
            btnAllCarts.bringToFront();
            btnAllCarts.setElevation(20f); // Android 5+ ke liye
        }
    }


    // ================= INIT =================
    private void initViews() {

        rvCategories = findViewById(R.id.rvCategories);
        rvRestaurants = findViewById(R.id.rvRestaurants);

        etSearch = findViewById(R.id.etSearch);
        tsSearchHint = findViewById(R.id.tsSearchHint);
        tvSearchPrefix = findViewById(R.id.tvSearchPrefix);

        emptyState = findViewById(R.id.layoutEmptyState);

        imgProfile = findViewById(R.id.imgProfile);
        btnAllCarts = findViewById(R.id.btnAllCarts); // ✅ ADD

    }


    // ================= PROFILE =================
    private void setupProfileClick() {

        if (imgProfile == null) return;

        imgProfile.setOnClickListener(v -> {

            Intent intent = new Intent(
                    HomeActivity.this,
                    ProfileActivity.class
            );

            startActivity(intent);
        });
    }


    // ================= RECYCLER =================
    private void setupRecyclerViews() {

        categoryAdapter = new CategoryAdapter();
        restaurantAdapter = new RestaurantAdapter();
        shimmerAdapter = new ShimmerRestaurantAdapter();

        rvCategories.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        rvCategories.setAdapter(categoryAdapter);

        rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        rvRestaurants.setAdapter(restaurantAdapter);

        categoryAdapter.setOnCategoryClickListener(this::filterByCategory);
    }


    // ================= VIEWMODEL =================
    private void setupViewModel() {

        HomeRepositoryImpl repository = new HomeRepositoryImpl();

        HomeViewModelFactory factory =
                new HomeViewModelFactory(
                        new GetBannersUseCase(repository),
                        new GetCategoriesUseCase(repository),
                        new GetRestaurantsUseCase(repository)
                );

        viewModel = new ViewModelProvider(this, factory)
                .get(HomeViewModel.class);
    }


    private void observeData() {

        viewModel.categories().observe(this, result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                categoryAdapter.submitList(result.data);
            }
        });

        viewModel.restaurants().observe(this, result -> {
            if (result.status == Resource.Status.SUCCESS && result.data != null) {

                allRestaurants.clear();
                allRestaurants.addAll(result.data);

                showResults(allRestaurants);

            } else if (result.status == Resource.Status.ERROR) {

                showError(result.message);
            }
        });
    }


    // ================= SEARCH =================
    private void setupSearch() {

        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Hide hint while typing
                tsSearchHint.setVisibility(View.GONE);
                tvSearchPrefix.setVisibility(View.GONE);

                if (pendingRunnable != null) {
                    handler.removeCallbacks(pendingRunnable);
                }

                showShimmer();

                pendingRunnable = () -> applySearch(s.toString());

                handler.postDelayed(pendingRunnable, 400);
            }

            @Override
            public void afterTextChanged(Editable s) {

                // Show hint again if empty
                if (s.toString().isEmpty()) {

                    tsSearchHint.setVisibility(View.VISIBLE);
                    tvSearchPrefix.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    private void applySearch(String query) {

        if (query == null || query.trim().isEmpty()) {
            showResults(allRestaurants);
            return;
        }

        String keyword = query.toLowerCase(Locale.ROOT);

        List<Restaurant> filtered = new ArrayList<>();

        for (Restaurant restaurant : allRestaurants) {

            if (restaurant.getName().toLowerCase().contains(keyword)
                    || restaurant.getLocation().toLowerCase().contains(keyword)) {

                filtered.add(restaurant);
                continue;
            }

            if (restaurant.getMenuItems() == null) continue;

            for (MenuItem item : restaurant.getMenuItems()) {

                if (item.getName().toLowerCase().contains(keyword)) {

                    filtered.add(restaurant);
                    break;
                }
            }
        }

        showResults(filtered);
    }


    // ================= ZOMATO HINT SWITCHER =================

    private void setupSearchHintSwitcher() {

        tsSearchHint.setFactory(() -> {

            TextView tv = new TextView(this);

            tv.setTextSize(16);
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

                    tsSearchHint.setText(zomatoHints[hintIndex]);

                    hintIndex++;

                    if (hintIndex >= zomatoHints.length) {
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


    // ================= CATEGORY =================
    private void filterByCategory(String rawCategory) {

        if (rawCategory == null) return;

        showShimmer();

        handler.postDelayed(() -> {

            String category = rawCategory.toLowerCase(Locale.ROOT);

            if (category.contains("all")) {
                showResults(allRestaurants);
                return;
            }

            List<Restaurant> filtered = new ArrayList<>();

            for (Restaurant restaurant : allRestaurants) {

                if (restaurant.getMenuItems() == null) continue;

                for (MenuItem item : restaurant.getMenuItems()) {

                    boolean match =
                            (category.contains("veg") && !category.contains("non") && item.isVeg()) ||
                                    (category.contains("non") && !item.isVeg()) ||
                                    (category.contains("biryani") && item.getName().toLowerCase().contains("biryani")) ||
                                    (category.contains("chicken") && item.getName().toLowerCase().contains("chicken")) ||
                                    (category.contains("fish") && item.getName().toLowerCase().contains("fish"));

                    if (match) {
                        filtered.add(restaurant);
                        break;
                    }
                }
            }

            showResults(filtered);

        }, 350);
    }


    // ================= UI =================
    private void showShimmer() {

        emptyState.setVisibility(View.GONE);

        rvRestaurants.setVisibility(View.VISIBLE);

        rvRestaurants.setAdapter(shimmerAdapter);
    }


    private void showResults(List<Restaurant> list) {

        rvRestaurants.setAdapter(restaurantAdapter);

        if (list == null || list.isEmpty()) {

            rvRestaurants.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);

        } else {

            emptyState.setVisibility(View.GONE);
            rvRestaurants.setVisibility(View.VISIBLE);

            restaurantAdapter.submitList(list);
        }
    }


    private void showError(String msg) {

        Toast.makeText(
                this,
                msg == null ? "Something went wrong" : msg,
                Toast.LENGTH_SHORT
        ).show();
    }


    private void showAllCartsBottomSheet() {

        List<String> carts =
                CartManager.getAllActiveRestaurants();

        if (carts.isEmpty()) return;

        BottomSheetDialog dialog =
                new BottomSheetDialog(this);

        View view = getLayoutInflater()
                .inflate(R.layout.bottomsheet_all_carts, null);

        dialog.setContentView(view);


        RecyclerView rv =
                view.findViewById(R.id.rvAllCarts);

        TextView btnClear =
                view.findViewById(R.id.btnClearAll);


        rv.setLayoutManager(
                new LinearLayoutManager(this));

        AllCartsAdapter adapter =
                new AllCartsAdapter(this, carts);

        rv.setAdapter(adapter);


        // Clear All
        btnClear.setOnClickListener(v -> {

            for (String id : carts) {
                CartManager.clearByRestaurant(id, this);
            }

            dialog.dismiss();
            updateAllCartsButton();
        });


        dialog.show();
    }

    private void showAllCartPopups() {

        List<String> restaurants =
                CartManager.getAllActiveRestaurants();

        if (restaurants == null || restaurants.isEmpty()) return;

        int delay = 0;

        for (String resId : restaurants) {

            int count = CartManager.getTotalItemsFor(resId);

            if (count <= 0) continue;

            final int d = delay;

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                showStackedPopup(resId, count);
            }, d);

            delay += 250; // stack animation feel
        }
    }

    /* ================= RESUME CART ================= */

    private void showStackedPopup(String resId, int count) {

        View root = findViewById(android.R.id.content);

        View popup =
                getLayoutInflater()
                        .inflate(R.layout.view_resume_cart, null);


        TextView tvName = popup.findViewById(R.id.tvRestaurantName);
        TextView tvCount = popup.findViewById(R.id.tvItemCount);

        TextView btnMenu = popup.findViewById(R.id.btnMenu);
        TextView btnCart = popup.findViewById(R.id.btnViewCart);

        ImageView btnClose = popup.findViewById(R.id.btnClose);


        tvName.setText("Your Cart");
        tvCount.setText(count + " items in cart");


        Snackbar snackbar =
                Snackbar.make(root, "", Snackbar.LENGTH_INDEFINITE);

        Snackbar.SnackbarLayout layout =
                (Snackbar.SnackbarLayout) snackbar.getView();

        layout.setBackgroundColor(
                getResources().getColor(android.R.color.transparent));

        layout.removeAllViews();
        layout.addView(popup);


        // VIEW CART
        btnCart.setOnClickListener(v -> {

            Intent i =
                    new Intent(this, CheckoutActivity.class);

            i.putExtra("RESTAURANT_ID", resId);

            startActivity(i);

            snackbar.dismiss();
        });


        // MENU
        btnMenu.setOnClickListener(v -> {

            Intent i =
                    new Intent(this,
                            com.example.messmateapp.ui.menu.MenuActivity.class);

            i.putExtra("RESTAURANT_ID", resId);

            startActivity(i);

            snackbar.dismiss();
        });


        // CLOSE
        btnClose.setOnClickListener(v -> snackbar.dismiss());


        snackbar.show();

        bringAllCartsToFront(); // ✅ ADD THIS

    }


    // ================= CLEANUP =================
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (hintHandler != null && hintRunnable != null) {
            hintHandler.removeCallbacks(hintRunnable);
        }
    }
}
