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
import com.example.messmateapp.data.model.CreateOrderResponse;
import com.example.messmateapp.data.model.VerifyResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class CheckoutActivity extends AppCompatActivity
        implements AddressBottomSheetAdapter.OnAddressSelectListener,
        PaymentResultListener {



    /* ================= UI ================= */

    private Button btnPlaceOrder, btnSelectAddress;

    private LinearLayout layoutAddMore, layoutTotalBill;

    private RecyclerView rvCart, rvRecommend;

    private TextView tvRecommendTitle, tvFinalAmount;
    private TextView tvMessName;

    private LinearLayout layoutPhoneEdit;
    private TextView tvBillPhone;

    private TextView tvAddress;
    private TextView tvChangeAddress;

    // ✅ Payment
    private RadioGroup rgPayment;
    private RadioButton rbOnline, rbCOD;
    private String selectedPayment = "";
    private ImageView btnChangePayment;
    private LinearLayout layoutPaymentAction;


    /* ================= Adapter ================= */

    private CartAdapter cartAdapter;
    private RecommendationAdapter recommendationAdapter;


    /* ================= Data ================= */

    private String messId = "";
    private String messName = "";
    private String messImage = ""; // ✅

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
        CartManager.setRestaurant(
                messId,
                messName,
                messImage,   // ✅ ADD THIS
                this
        );

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
            messImage = getIntent().getStringExtra("RESTAURANT_IMAGE");

            if (messImage == null) messImage = "";

            if (messId == null) messId = "";
            if (messName == null) messName = "";
        }
    }


    /* ================= Init Views ================= */

    private void initViews() {

        // 🔥 Payment Action Layout (Button + Edit Icon)
        layoutPaymentAction = findViewById(R.id.layoutPaymentAction);

        // 🔥 Main Button
        btnPlaceOrder = findViewById(R.id.btnSelectPayment);

        // 🔥 Change Payment Icon
        btnChangePayment = findViewById(R.id.btnChangePayment);


        // Address Button
        btnSelectAddress = findViewById(R.id.btnSelectAddress);

        layoutAddMore = findViewById(R.id.layoutAddMore);

        rvCart = findViewById(R.id.rvCart);
        rvRecommend = findViewById(R.id.rvRecommend);

        tvRecommendTitle = findViewById(R.id.tvRecommendTitle);

        tvMessName = findViewById(R.id.tvMessName);

        tvAddress = findViewById(R.id.tvAddress);
        tvChangeAddress = findViewById(R.id.tvChangeAddress);


        View card = findViewById(R.id.cardTotalBill);

        layoutTotalBill = card.findViewById(R.id.layoutTotalBill);
        tvFinalAmount = card.findViewById(R.id.tvFinalAmount);

        layoutPhoneEdit = card.findViewById(R.id.layoutPhoneEdit);
        tvBillPhone = card.findViewById(R.id.tvBillPhone);


        // Set Restaurant Name
        if (!messName.isEmpty()) {
            tvMessName.setText(messName);
        }


        // Click Listeners
        layoutTotalBill.setOnClickListener(v -> showBillPopup());
        layoutPhoneEdit.setOnClickListener(v -> showEditPhoneDialog());


        // Change Address
        if (tvChangeAddress != null) {
            tvChangeAddress.setOnClickListener(v -> openAddressBottomSheet());
        }


        // 🔥 Default UI State
        btnPlaceOrder.setText("Select Payment Method");

        // Hide edit icon initially
        btnChangePayment.setVisibility(View.GONE);
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
                        messId,        // ✅ pass restaurant id
                        messName,      // ✅ pass restaurant name
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

            // 🔥 FIRST TIME → Open BottomSheet
            if (selectedPayment.isEmpty()) {
                showPaymentBottomSheet();
                return;
            }

            // 🔥 AFTER SELECT
            if (selectedPayment.equals("COD")) {

                placeOrderCOD();

            } else {

                startPaymentFlow();
            }
        });


        // Address BottomSheet
        btnSelectAddress.setOnClickListener(v ->
                openAddressBottomSheet()
        );
// 🔁 Change Payment Anytime

        btnChangePayment.setOnClickListener(v -> {

            showPaymentBottomSheet();
        });


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

    private void showPaymentBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);

        View view = getLayoutInflater()
                .inflate(R.layout.bottom_payment, null);

        dialog.setContentView(view);

        RadioGroup rg = view.findViewById(R.id.radioGroupPayment);

        Button btnConfirm = view.findViewById(R.id.btnConfirmPayment);

        btnConfirm.setOnClickListener(v -> {

            int id = rg.getCheckedRadioButtonId();

            if (id == -1) {
                Toast.makeText(this,
                        "Select payment method",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (id == R.id.radioOnline) {
                selectedPayment = "ONLINE";
            } else {
                selectedPayment = "COD";
            }

            dialog.dismiss();

            updatePaymentButton();
        });

        dialog.show();
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

        order.paymentMethod = selectedPayment;
        order.total_price = finalGrandTotal;


        // ✅ Send full address object
        order.deliveryAddress = selectedAddress;


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
// ✅ Receiver Info
        order.receiverName = getUserName();
        order.receiverPhone = getUserPhone();


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

    // ================= USER INFO =================

    private String getUserName() {

        SharedPreferences pref =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        return pref.getString(KEY_NAME, "Guest");
    }


    private String getUserPhone() {

        SharedPreferences pref =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        return pref.getString(KEY_PHONE, "");
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

        // Set locally
        selectedAddress = address;

        // Update UI
        updateAddressUI(address);

        updateBottomButton();

        // Repo
        AddressRepositoryImpl repo =
                new AddressRepositoryImpl(this);

        // ✅ Call backend select API
        repo.selectAddress(address.getId())
                .observe(this, res -> {

                    if (res.isLoading()) return;

                    if (res.isSuccess() && res.getData() != null) {

                        // Update selected address from backend
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

        // Label
        if (a.getLabel() != null && !a.getLabel().isEmpty()) {
            sb.append(a.getLabel()).append(" • ");
        }

        // House
        if (a.getHouse() != null && !a.getHouse().isEmpty()) {
            sb.append(a.getHouse()).append(", ");
        }

        // Area
        if (a.getArea() != null && !a.getArea().isEmpty()) {
            sb.append(a.getArea()).append(", ");
        }

        // Landmark
        if (a.getLandmark() != null && !a.getLandmark().isEmpty()) {
            sb.append(a.getLandmark()).append(", ");
        }

        // City
        if (a.getCity() != null && !a.getCity().isEmpty()) {
            sb.append(a.getCity());
        }

        // Pincode
        if (a.getPincode() != null && !a.getPincode().isEmpty()) {
            sb.append(" - ").append(a.getPincode());
        }

        tvAddress.setText(sb.toString());

        if (tvChangeAddress != null) {
            tvChangeAddress.setVisibility(View.VISIBLE);
        }

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
    /* ================= PAYMENT FLOW ================= */

    private void startPaymentFlow() {

        btnPlaceOrder.setEnabled(false);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", finalGrandTotal);

        ApiClient.getClient()
                .create(ApiService.class)
                .createOrder(body)
                .enqueue(new Callback<CreateOrderResponse>() {

                    @Override
                    public void onResponse(Call<CreateOrderResponse> call,
                                           Response<CreateOrderResponse> response) {

                        btnPlaceOrder.setEnabled(true);

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success) {

                            openRazorpay(response.body());
                        } else {

                            Toast.makeText(
                                    CheckoutActivity.this,
                                    "Payment Init Failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CreateOrderResponse> call,
                                          Throwable t) {

                        btnPlaceOrder.setEnabled(true);

                        Toast.makeText(
                                CheckoutActivity.this,
                                "Network Error",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }


    private void openRazorpay(CreateOrderResponse res) {

        Checkout checkout = new Checkout();
        checkout.setKeyID(res.key);

        try {

            JSONObject options = new JSONObject();

            options.put("name", "MessMate");
            options.put("description", "Food Order Payment");
            options.put("order_id", res.order.id);
            options.put("currency", "INR");
            options.put("amount", res.order.amount);

            checkout.open(this, options);

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(this,
                    "Payment Error",
                    Toast.LENGTH_SHORT).show();
        }
    }


    /* ================= PAYMENT CALLBACK ================= */

    @Override
    public void onPaymentSuccess(String paymentId) {

        verifyPayment(paymentId);
    }


    @Override
    public void onPaymentError(int code, String response) {

        Toast.makeText(this,
                "Payment Failed",
                Toast.LENGTH_LONG).show();
    }

    private void verifyPayment(String paymentId) {

        // Razorpay automatically sends orderId + signature
        // You should get them via intent (advanced)
        // For now backend will verify

        Map<String, String> body = new HashMap<>();

        body.put("razorpay_payment_id", paymentId);

        ApiClient.getClient()
                .create(ApiService.class)
                .verifyPayment(body)
                .enqueue(new Callback<VerifyResponse>() {

                    @Override
                    public void onResponse(Call<VerifyResponse> call,
                                           Response<VerifyResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().success) {

                            // ✅ After payment → place order
                            placeOrder();
                        } else {

                            Toast.makeText(
                                    CheckoutActivity.this,
                                    "Payment Verification Failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<VerifyResponse> call,
                                          Throwable t) {

                        Toast.makeText(
                                CheckoutActivity.this,
                                "Verify Error",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void placeOrderCOD() {

        btnPlaceOrder.setEnabled(false);

        OrderRepositoryImpl repo =
                new OrderRepositoryImpl(this);

        OrderRequestDto order = new OrderRequestDto();

        order.mess_id = messId;
        order.mess_name = messName;

        order.paymentMethod = "COD";

        order.total_price = finalGrandTotal;

        // ✅ Send full address object
        order.deliveryAddress = selectedAddress;


        List<OrderRequestDto.CartItemDto> items =
                new ArrayList<>();

        for (CartItem c : cartList) {

            OrderRequestDto.CartItemDto dto =
                    new OrderRequestDto.CartItemDto();

            dto.name = c.getName();
            dto.price = c.getPrice();
            dto.quantity = c.getQuantity();

            items.add(dto);
        }

        order.items = items;
// ✅ Receiver Info
        order.receiverName = getUserName();
        order.receiverPhone = getUserPhone();


        repo.placeOrder(order)
                .observe(this, res -> {

                    // ✅ IGNORE LOADING
                    if (res.isLoading()) return;

                    btnPlaceOrder.setEnabled(true);

                    // ✅ SUCCESS
                    if (res.isSuccess()) {

                        Toast.makeText(
                                this,
                                "Order placed (COD)",
                                Toast.LENGTH_SHORT
                        ).show();

                        CartManager.clear(this);

                        finish();

                    }

                    // ❌ REAL ERROR
                    else {

                        String msg = res.getMessage();

                        if (msg == null || msg.isEmpty()) {
                            msg = "COD Failed";
                        }

                        Toast.makeText(
                                this,
                                msg,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
// ================= PAYMENT BUTTON UPDATE =================

    private void updatePaymentButton() {

        btnPlaceOrder.setText(
                "Place Order (" + selectedPayment + ")"
        );

        // Show edit icon after selection
        btnChangePayment.setVisibility(View.VISIBLE);
    }
}
