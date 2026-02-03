package com.example.messmateapp.ui.cart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmateapp.R;
import com.example.messmateapp.data.model.AddressDto;
import com.example.messmateapp.data.model.OrderRequestDto;
import com.example.messmateapp.data.model.RecommendationResponse;
import com.example.messmateapp.data.remote.ApiClient;
import com.example.messmateapp.data.remote.ApiService;
import com.example.messmateapp.data.repository.AddressRepositoryImpl;
import com.example.messmateapp.data.repository.OrderRepositoryImpl;
import com.example.messmateapp.domain.model.CartItem;
import com.example.messmateapp.ui.menu.MenuActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class CheckoutActivity extends AppCompatActivity
        implements AddressBottomSheetAdapter.OnAddressSelectListener {


    /* ================= UI ================= */

    private Button btnPlaceOrder, btnSelectAddress;

    private LinearLayout layoutAddMore, layoutTotalBill;

    private RecyclerView rvCart, rvRecommend;

    private TextView tvRecommendTitle, tvFinalAmount;
    private TextView tvMessName;

    private LinearLayout layoutPhoneEdit;
    private TextView tvBillPhone;

    private TextView tvAddress;
    private TextView tvChangeAddress; // ✅ NEW




    /* ================= Adapter ================= */

    private CartAdapter cartAdapter;
    private RecommendationAdapter recommendationAdapter;


    /* ================= Data ================= */

    private String messId = "";
    private String messName = "";

    private int finalGrandTotal = 0;

    private final List<CartItem> cartList = new ArrayList<>();
    private final List<RecommendationResponse.RecommendationItem> recommendList =
            new ArrayList<>();

    private AddressDto selectedAddress;
    private List<AddressDto> cachedAddresses = new ArrayList<>();



    /* ================= PREF ================= */

    private static final String PREF_NAME = "USER_DATA";
    private static final String KEY_PHONE = "USER_PHONE";
    private static final String KEY_NAME = "USER_NAME";


    /* ================= LifeCycle ================= */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initIntent();

// 🔥 IMPORTANT: Set current restaurant cart (with context)
        CartManager.setRestaurant(messId, this);

        initViews();


        setupCart();
        setupRecommendation();
        setupClickListeners();

        loadSavedUser();
        loadDefaultAddress();

        refreshAll();
    }


    @Override
    protected void onResume() {
        super.onResume();

        loadDefaultAddress();
    }




    /* ================= Init Intent ================= */

    private void initIntent() {

        if (getIntent() != null) {

            messId = getIntent().getStringExtra("RESTAURANT_ID");
            messName = getIntent().getStringExtra("RESTAURANT_NAME");

            if (messId == null) messId = "";
            if (messName == null) messName = "";
        }
    }


    /* ================= Init Views ================= */

    private void initViews() {

        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnSelectAddress = findViewById(R.id.btnSelectAddress);

        layoutAddMore = findViewById(R.id.layoutAddMore);

        rvCart = findViewById(R.id.rvCart);
        rvRecommend = findViewById(R.id.rvRecommend);

        tvRecommendTitle = findViewById(R.id.tvRecommendTitle);

        tvMessName = findViewById(R.id.tvMessName);

        tvAddress = findViewById(R.id.tvAddress);
        tvChangeAddress = findViewById(R.id.tvChangeAddress); // ✅ IMPORTANT


        View card = findViewById(R.id.cardTotalBill);

        layoutTotalBill = card.findViewById(R.id.layoutTotalBill);
        tvFinalAmount = card.findViewById(R.id.tvFinalAmount);

        layoutPhoneEdit = card.findViewById(R.id.layoutPhoneEdit);
        tvBillPhone = card.findViewById(R.id.tvBillPhone);


        if (!messName.isEmpty()) {
            tvMessName.setText(messName);
        }


        layoutTotalBill.setOnClickListener(v -> showBillPopup());
        layoutPhoneEdit.setOnClickListener(v -> showEditPhoneDialog());


        // ✅ Change Address Click
        if (tvChangeAddress != null) {
            tvChangeAddress.setOnClickListener(v -> openAddressBottomSheet());
        }
    }



    /* ================= Button Control ================= */

    private void updateBottomButton() {

        if (selectedAddress == null) {

            btnSelectAddress.setVisibility(View.VISIBLE);
            btnPlaceOrder.setVisibility(View.GONE);

        } else {

            btnSelectAddress.setVisibility(View.GONE);
            btnPlaceOrder.setVisibility(View.VISIBLE);
        }
    }


    /* ================= Load Active Address ================= */

    private void loadDefaultAddress() {

        AddressRepositoryImpl repo =
                new AddressRepositoryImpl(this);

        repo.getActiveAddress().observe(this, res -> {

            if (res.isLoading()) return;

            if (!res.isSuccess() || res.getData() == null) {

                clearAddress();
                return;
            }

            selectedAddress = res.getData();

            updateAddressUI(selectedAddress);
            updateBottomButton();
        });
    }



    /* ================= Load User ================= */

    private void loadSavedUser() {

        SharedPreferences pref =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        String name = pref.getString(KEY_NAME, "Guest");
        String phone = pref.getString(KEY_PHONE, "");


        if (!phone.isEmpty()) {
            tvBillPhone.setText(name + ", " + phone);
        } else {
            tvBillPhone.setText(name);
        }
    }


    /* ================= Setup Cart ================= */

    private void setupCart() {

        rvCart.setLayoutManager(new LinearLayoutManager(this));

        cartAdapter =
                new CartAdapter(this, cartList, this::refreshAll);


        rvCart.setAdapter(cartAdapter);
    }


    /* ================= Setup Recommendation ================= */

    private void setupRecommendation() {

        rvRecommend.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        recommendationAdapter =
                new RecommendationAdapter(
                        this,
                        recommendList,
                        this::refreshAll
                );

        rvRecommend.setAdapter(recommendationAdapter);
    }


    /* ================= Refresh ================= */

    private void refreshAll() {

        refreshCart();
        loadRecommendations();
        updateBill();
    }


    private void refreshCart() {

        cartList.clear();
        cartList.addAll(CartManager.getItems());

        cartAdapter.notifyDataSetChanged();
    }


    /* ================= Click Listeners ================= */

    private void setupClickListeners() {

        btnPlaceOrder.setOnClickListener(v -> {

            if (cartList.isEmpty()) {

                Toast.makeText(this,
                        "Cart is empty",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedAddress == null) {

                Toast.makeText(this,
                        "Select address first",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            placeOrder();
        });


        // ✅ Only BottomSheet (No Address Activity)

        btnSelectAddress.setOnClickListener(v ->
                openAddressBottomSheet()
        );


        layoutAddMore.setOnClickListener(v -> {

            Intent intent =
                    new Intent(this, MenuActivity.class);

            intent.putExtra("RESTAURANT_ID", messId);
            intent.putExtra("RESTAURANT_NAME", messName);

            startActivity(intent);
        });
    }


    /* ================= Recommendation ================= */

    private void loadRecommendations() {

        if (messId.isEmpty()) return;


        ApiService api =
                ApiClient.getClient()
                        .create(ApiService.class);


        api.getMessRecommendations(messId)
                .enqueue(new Callback<RecommendationResponse>() {

                    @Override
                    public void onResponse(Call<RecommendationResponse> call,
                                           Response<RecommendationResponse> res) {

                        if (!res.isSuccessful()
                                || res.body() == null
                                || !res.body().success) {

                            hideRecommendations();
                            return;
                        }


                        recommendList.clear();

                        Set<String> cartNames = new HashSet<>();

                        for (CartItem c : cartList) {

                            if (c.getName() != null) {
                                cartNames.add(c.getName().toLowerCase());
                            }
                        }


                        for (RecommendationResponse.RecommendationItem item
                                : res.body().data) {

                            if (item == null || item.name == null) continue;

                            if (!cartNames.contains(item.name.toLowerCase())) {

                                recommendList.add(item);
                            }
                        }

                        updateRecommendationUI();
                    }


                    @Override
                    public void onFailure(Call<RecommendationResponse> call,
                                          Throwable t) {

                        hideRecommendations();
                    }
                });
    }


    private void updateRecommendationUI() {

        if (recommendList.isEmpty()) {

            hideRecommendations();

        } else {

            rvRecommend.setVisibility(View.VISIBLE);
            tvRecommendTitle.setVisibility(View.VISIBLE);

            recommendationAdapter.notifyDataSetChanged();
        }
    }


    private void hideRecommendations() {

        rvRecommend.setVisibility(View.GONE);
        tvRecommendTitle.setVisibility(View.GONE);
    }


    /* ================= Update Bill ================= */

    private void updateBill() {

        int total = 0;

        for (CartItem item : cartList) {

            total += item.getPrice() * item.getQuantity();
        }

        int deliveryFee = total > 300 ? 0 : 30;
        int platformFee = 5;
        int gst = (int) (total * 0.05);


        finalGrandTotal =
                total + deliveryFee + platformFee + gst;

        tvFinalAmount.setText("₹" + finalGrandTotal);
    }


    /* ================= Place Order ================= */

    private void placeOrder() {

        // Disable button to avoid double click
        btnPlaceOrder.setEnabled(false);

        OrderRepositoryImpl repo =
                new OrderRepositoryImpl(this);


        OrderRequestDto order = new OrderRequestDto();

        order.mess_id = messId;
        order.mess_name = messName;

        order.paymentMethod = "Online";
        order.total_price = finalGrandTotal;


        order.deliveryAddress =
                selectedAddress.house + ", " +
                        selectedAddress.area + ", " +
                        selectedAddress.city + " - " +
                        selectedAddress.pincode;


        order.lat = selectedAddress.lat;
        order.lng = selectedAddress.lng;


        List<OrderRequestDto.CartItemDto> items =
                new ArrayList<>();


        for (CartItem c : cartList) {

            OrderRequestDto.CartItemDto dto =
                    new OrderRequestDto.CartItemDto();

            dto.name = c.getName();
            dto.price = c.getPrice();
            dto.quantity = c.getQuantity();
            dto.image = c.getImage();
            dto.type = c.getType();
            dto.category = c.getCategory();

            items.add(dto);
        }

        order.items = items;


        // ✅ Observe API Result
        repo.placeOrder(order)
                .observe(this, res -> {

                    // Loading
                    if (res.isLoading()) {
                        return;
                    }

                    // Re-enable button
                    btnPlaceOrder.setEnabled(true);


                    // ✅ Success
                    if (res.isSuccess()) {

                        Toast.makeText(
                                this,
                                "✅ Order Placed Successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                        // ✅ Clear Cart (and clear from storage)
                        CartManager.clear(this);


                        // ✅ Go Back to Menu (Fresh)
                        Intent intent = new Intent(CheckoutActivity.this, MenuActivity.class);

                        intent.putExtra("RESTAURANT_ID", messId);
                        intent.putExtra("RESTAURANT_NAME", messName);

                        // ✅ Clear old stack
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);

                        finish();
                    }


                    // ❌ Error
                    else {

                        String msg = res.getMessage();

                        if (msg == null || msg.isEmpty()) {
                            msg = "Order failed. Try again.";
                        }

                        Toast.makeText(
                                this,
                                "❌ " + msg,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }


    /* ================= Phone Dialog ================= */

    private void showEditPhoneDialog() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);

        View view = getLayoutInflater()
                .inflate(R.layout.dialog_edit_phone, null);

        dialog.setContentView(view);


        EditText etName = view.findViewById(R.id.etName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        Button btnSave = view.findViewById(R.id.btnSavePhone);


        SharedPreferences pref =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);


        etName.setText(pref.getString(KEY_NAME, "Guest"));


        String savedPhone = pref.getString(KEY_PHONE, "");

        if (!savedPhone.isEmpty()) {
            etPhone.setText(savedPhone.replace("+91 ", ""));
        }


        btnSave.setOnClickListener(v -> {

            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();


            if (name.isEmpty()) {
                etName.setError("Enter name");
                return;
            }

            if (phone.length() != 10) {
                etPhone.setError("Enter valid number");
                return;
            }


            pref.edit()
                    .putString(KEY_NAME, name)
                    .putString(KEY_PHONE, "+91 " + phone)
                    .apply();


            loadSavedUser();

            dialog.dismiss();


            Toast.makeText(this,
                    "Details updated",
                    Toast.LENGTH_SHORT).show();
        });


        dialog.show();
    }

    /* ================= Address Bottom Sheet ================= */

    private void openAddressBottomSheet() {

        BottomSheetDialog dialog =
                new BottomSheetDialog(this);

        View view = LayoutInflater.from(this)
                .inflate(R.layout.bottomsheet_address_picker, null);

        dialog.setContentView(view);


        /* ================= Close Button ================= */

        ImageView btnClose = view.findViewById(R.id.btnClose);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }


        /* ================= Add Address Button ================= */

        LinearLayout addBtn = view.findViewById(R.id.layoutAddAddress);

        if (addBtn != null) {

            addBtn.setOnClickListener(v -> {

                dialog.dismiss();

                // Open Add Address Screen
                startActivity(
                        new Intent(
                                CheckoutActivity.this,
                                com.example.messmateapp.ui.address.AddAddressActivity.class
                        )
                );
            });
        }


        /* ================= Force Height ================= */

        dialog.setOnShowListener(d -> {

            View bottomSheet =
                    ((BottomSheetDialog) d).findViewById(
                            com.google.android.material.R.id.design_bottom_sheet
                    );

            if (bottomSheet != null) {

                BottomSheetBehavior<View> behavior =
                        BottomSheetBehavior.from(bottomSheet);

                int height =
                        (int) (getResources().getDisplayMetrics().heightPixels * 0.7);

                bottomSheet.getLayoutParams().height = height;
                bottomSheet.requestLayout();

                behavior.setPeekHeight(height);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setDraggable(true);
            }
        });


        /* ================= RecyclerView ================= */

        RecyclerView rv =
                view.findViewById(R.id.rvAddresses);

        rv.setLayoutManager(new LinearLayoutManager(this));


        /* ================= Load Addresses ================= */

        AddressRepositoryImpl repo =
                new AddressRepositoryImpl(this);

        repo.getAddresses().observe(this, res -> {

            if (res.isLoading()) return;

            if (!res.isSuccess() || res.getData() == null) {

                Toast.makeText(
                        this,
                        "Failed to load addresses",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (res.getData().isEmpty()) {

                Toast.makeText(
                        this,
                        "No address found. Add one first.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            cachedAddresses.clear();
            cachedAddresses.addAll(res.getData());

            AddressBottomSheetAdapter adapter =
                    new AddressBottomSheetAdapter(
                            cachedAddresses,
                            this,
                            dialog
                    );

            rv.setAdapter(adapter);
        });

        /* ================= Show ================= */

        dialog.show();
    }


    @Override
    public void onAddressSelected(AddressDto address) {

        selectedAddress = address;

        updateAddressUI(address);

        updateBottomButton();

        AddressRepositoryImpl repo =
                new AddressRepositoryImpl(this);

        // ✅ Call backend select API
        repo.selectAddress(address._id)
                .observe(this, res -> {

                    if (res.isLoading()) return;

                    if (res.isSuccess() && res.getData() != null) {

                        selectedAddress = res.getData();

                        updateAddressUI(selectedAddress);

                        Toast.makeText(
                                this,
                                "Address selected",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                this,
                                "Failed to select address",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }


    /* ================= Helpers ================= */

    private void clearAddress() {

        selectedAddress = null;

        // Reset text
        tvAddress.setText("Add delivery address");

        // ✅ Hide Change button
        if (tvChangeAddress != null) {
            tvChangeAddress.setVisibility(View.GONE);
        }

        // Refresh bottom button
        updateBottomButton();
    }



    private void updateAddressUI(AddressDto a) {

        if (a == null) {
            clearAddress();
            return;
        }

        StringBuilder sb = new StringBuilder();

        // Label (Home / Work etc.)
        if (a.label != null && !a.label.isEmpty()) {
            sb.append(a.label).append(" • ");
        }

        // House / Flat
        if (a.house != null && !a.house.isEmpty()) {
            sb.append(a.house).append(", ");
        }

        // Area
        if (a.area != null && !a.area.isEmpty()) {
            sb.append(a.area).append(", ");
        }

        // Landmark (Optional)
        if (a.landmark != null && !a.landmark.isEmpty()) {
            sb.append(a.landmark).append(", ");
        }

        // City
        if (a.city != null && !a.city.isEmpty()) {
            sb.append(a.city);
        }

        // Pincode
        if (a.pincode != null && !a.pincode.isEmpty()) {
            sb.append(" - ").append(a.pincode);
        }

        // Set Address Text
        tvAddress.setText(sb.toString());

        // ✅ Show "Change" button if exists
        if (tvChangeAddress != null) {
            tvChangeAddress.setVisibility(View.VISIBLE);
        }

        // Refresh bottom button
        updateBottomButton();
    }


    /* ================= Bill Popup ================= */

    private void showBillPopup() {

        if (cartList == null || cartList.isEmpty()) return;


        BottomSheetDialog dialog =
                new BottomSheetDialog(this);


        View view = getLayoutInflater()
                .inflate(R.layout.dialog_bill_summary, null);

        dialog.setContentView(view);


        TextView tvItem = view.findViewById(R.id.tvSheetItem);
        TextView tvDelivery = view.findViewById(R.id.tvSheetDelivery);
        TextView tvPlatform = view.findViewById(R.id.tvSheetPlatform);
        TextView tvGst = view.findViewById(R.id.tvSheetGst);
        TextView tvTotal = view.findViewById(R.id.tvSheetTotal);

        ImageView btnClose = view.findViewById(R.id.btnCloseSheet);


        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }


        int total = 0;

        for (CartItem item : cartList) {

            if (item != null) {
                total += item.getPrice() * item.getQuantity();
            }
        }


        int deliveryFee = total > 300 ? 0 : 30;
        int platformFee = 5;
        int gst = (int) (total * 0.05);


        int grandTotal =
                total + deliveryFee + platformFee + gst;


        tvItem.setText("₹" + total);
        tvDelivery.setText("₹" + deliveryFee);
        tvPlatform.setText("₹" + platformFee);
        tvGst.setText("₹" + gst);
        tvTotal.setText("₹" + grandTotal);


        dialog.show();
    }

}
