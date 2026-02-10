package com.example.messmateapp.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.messmateapp.data.model.OrderRequestDto;
import com.example.messmateapp.data.model.OrderHistoryResponse;
import com.example.messmateapp.domain.model.CartItem;
import com.example.messmateapp.domain.model.ReorderResponse;
import com.example.messmateapp.data.remote.ApiClient;
import com.example.messmateapp.data.remote.ApiService;
import com.example.messmateapp.domain.repository.OrderRepository;
import com.example.messmateapp.ui.cart.CartManager;
import com.example.messmateapp.utils.Resource;
import com.example.messmateapp.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderRepositoryImpl implements OrderRepository {

    private final ApiService apiService;
    private final SessionManager sessionManager;
    private final Context context;   // ✅ SAVE CONTEXT


    public OrderRepositoryImpl(Context context) {

        this.context = context;

        sessionManager = new SessionManager(context);

        apiService =
                ApiClient
                        .getAuthClient(context)
                        .create(ApiService.class);
    }


    /* ================= PLACE ORDER ================= */

    @Override
    public LiveData<Resource<Object>> placeOrder(OrderRequestDto order) {

        MutableLiveData<Resource<Object>> liveData =
                new MutableLiveData<>();

        liveData.setValue(Resource.loading());


        if (order == null) {

            Log.e("ORDER", "❌ Order is NULL");

            liveData.setValue(
                    Resource.error("Order is empty")
            );

            return liveData;
        }


        String token = sessionManager.getToken();

        if (token == null || token.trim().isEmpty()) {

            Log.e("ORDER", "❌ Token missing");

            liveData.setValue(
                    Resource.error("Session expired. Login again.")
            );

            return liveData;
        }


        apiService.placeOrder(order)
                .enqueue(new Callback<Object>() {

                    @Override
                    public void onResponse(
                            Call<Object> call,
                            Response<Object> response) {

                        if (response.isSuccessful()) {

                            Log.d("ORDER", "✅ Order placed");

                            liveData.setValue(
                                    Resource.success(response.body())
                            );

                        } else {

                            Log.e("ORDER",
                                    "❌ Failed: " + response.code());

                            liveData.setValue(
                                    Resource.error("Order failed")
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Object> call,
                            Throwable t) {

                        Log.e("ORDER", "💥 API ERROR", t);

                        liveData.setValue(
                                Resource.error("Network error")
                        );
                    }
                });

        return liveData;
    }


    /* ================= GET MY ORDERS ================= */

    @Override
    public Call<OrderHistoryResponse> getMyOrders() {

        String token = sessionManager.getToken();

        return apiService.getMyOrders(
                "Bearer " + token
        );
    }


    /* ================= REORDER (FINAL FIXED) ================= */

    @Override
    public LiveData<Resource<List<CartItem>>> reorderOrder(String orderId) {

        MutableLiveData<Resource<List<CartItem>>> liveData =
                new MutableLiveData<>();

        liveData.setValue(Resource.loading());


        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {

            Log.e("REORDER", "❌ Token missing");

            liveData.setValue(
                    Resource.error("Session expired")
            );

            return liveData;
        }


        Log.d("REORDER", "📤 Calling reorder API: " + orderId);


        apiService.reorderOrder(orderId)
                .enqueue(new Callback<ReorderResponse>() {

                    @Override
                    public void onResponse(
                            Call<ReorderResponse> call,
                            Response<ReorderResponse> response) {

                        Log.d("REORDER",
                                "📥 Response Code = " + response.code());


                        /* ================= BASIC CHECK ================= */

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            Log.e("REORDER", "❌ Empty Response");

                            liveData.setValue(
                                    Resource.error("Reorder failed")
                            );
                            return;
                        }


                        ReorderResponse data = response.body();


                        /* ================= NULL SAFETY ================= */

                        if (!data.isSuccess()) {

                            Log.e("REORDER", "❌ API Success = false");

                            liveData.setValue(
                                    Resource.error("Reorder failed")
                            );
                            return;
                        }


                        if (data.getRestaurant() == null) {

                            Log.e("REORDER", "❌ Restaurant is NULL");

                            liveData.setValue(
                                    Resource.error("Restaurant not found")
                            );
                            return;
                        }


                        if (data.getCartItems() == null
                                || data.getCartItems().isEmpty()) {

                            Log.e("REORDER", "❌ CartItems EMPTY");

                            liveData.setValue(
                                    Resource.error("No items found")
                            );
                            return;
                        }


                        /* ================= DEBUG ================= */

                        Log.d("REORDER", "✅ Restaurant: "
                                + data.getRestaurant().getName());

                        Log.d("REORDER", "✅ Items Count: "
                                + data.getCartItems().size());


                        /* ================= SET CART ================= */

                        CartManager.setCartForRestaurant(

                                data.getRestaurant().getId(),
                                data.getCartItems(),
                                data.getRestaurant().getName(),
                                data.getRestaurant().getBanner(),

                                context
                        );


                        Log.d("REORDER", "🛒 Cart Updated Successfully");


                        /* ================= SUCCESS ================= */

                        liveData.setValue(
                                Resource.success(
                                        data.getCartItems()
                                )
                        );
                    }


                    @Override
                    public void onFailure(
                            Call<ReorderResponse> call,
                            Throwable t) {

                        Log.e("REORDER", "💥 API ERROR", t);

                        liveData.setValue(
                                Resource.error("Network error")
                        );
                    }
                });

        return liveData;
    }
}
