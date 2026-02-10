package com.example.messmateapp.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.messmateapp.data.model.OrderRequestDto;
import com.example.messmateapp.data.model.OrderHistoryResponse;
import com.example.messmateapp.domain.model.CartItem;
import com.example.messmateapp.data.remote.ApiClient;
import com.example.messmateapp.data.remote.ApiService;
import com.example.messmateapp.domain.repository.OrderRepository;
import com.example.messmateapp.utils.Resource;
import com.example.messmateapp.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderRepositoryImpl implements OrderRepository {

    private final ApiService apiService;
    private final SessionManager sessionManager;


    public OrderRepositoryImpl(Context context) {

        sessionManager = new SessionManager(context);

        // Auth client (Token handled by interceptor)
        apiService =
                ApiClient
                        .getAuthClient(context)
                        .create(ApiService.class);
    }


    // ================= PLACE ORDER =================

    @Override
    public LiveData<Resource<Object>> placeOrder(OrderRequestDto order) {

        MutableLiveData<Resource<Object>> liveData =
                new MutableLiveData<>();

        liveData.setValue(Resource.loading());


        /* ================= Validation ================= */

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


        /* ================= API Call ================= */

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
                                    Resource.error("Order failed. Try again.")
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Object> call,
                            Throwable t) {

                        Log.e("ORDER", "💥 API ERROR", t);

                        liveData.setValue(
                                Resource.error(
                                        t.getMessage() != null
                                                ? t.getMessage()
                                                : "Network error"
                                )
                        );
                    }
                });

        return liveData;
    }


    // ================= GET MY ORDERS =================

    @Override
    public Call<OrderHistoryResponse> getMyOrders() {

        String token = sessionManager.getToken();

        return apiService.getMyOrders(
                "Bearer " + token
        );
    }


    // ================= REORDER (LATEST DATA) =================
    // 🔥 NEW METHOD (Important for Zomato Flow)

    @Override
    public LiveData<Resource<List<CartItem>>> reorderOrder(String orderId) {

        MutableLiveData<Resource<List<CartItem>>> liveData =
                new MutableLiveData<>();

        liveData.setValue(Resource.loading());


        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {

            liveData.setValue(
                    Resource.error("Session expired")
            );

            return liveData;
        }


        apiService.reorderOrder(orderId)
                .enqueue(new Callback<List<CartItem>>() {

                    @Override
                    public void onResponse(
                            Call<List<CartItem>> call,
                            Response<List<CartItem>> response) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            Log.d("REORDER", "✅ Reorder data fetched");

                            liveData.setValue(
                                    Resource.success(response.body())
                            );

                        } else {

                            Log.e("REORDER",
                                    "❌ Failed: " + response.code());

                            liveData.setValue(
                                    Resource.error("Reorder failed")
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<CartItem>> call,
                            Throwable t) {

                        Log.e("REORDER", "💥 API ERROR", t);

                        liveData.setValue(
                                Resource.error(
                                        t.getMessage() != null
                                                ? t.getMessage()
                                                : "Network error"
                                )
                        );
                    }
                });

        return liveData;
    }
}
