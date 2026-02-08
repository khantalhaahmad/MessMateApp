package com.example.messmateapp.domain.repository;

import androidx.lifecycle.LiveData;

import com.example.messmateapp.data.model.OrderHistoryResponse;
import com.example.messmateapp.data.model.OrderRequestDto;
import com.example.messmateapp.utils.Resource;

import retrofit2.Call;

/**
 * Handles Order related operations
 */
public interface OrderRepository {

    /**
     * Place order
     */
    LiveData<Resource<Object>> placeOrder(OrderRequestDto order);


    /**
     * Get user order history
     */
    Call<OrderHistoryResponse> getMyOrders();
}
