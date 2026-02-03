package com.example.messmateapp.domain.repository;

import androidx.lifecycle.LiveData;

import com.example.messmateapp.data.model.OrderRequestDto;
import com.example.messmateapp.utils.Resource;

/**
 * Handles Order related operations
 */
public interface OrderRepository {

    /**
     * Place order (JWT handled internally via SessionManager)
     */
    LiveData<Resource<Object>> placeOrder(OrderRequestDto order);
}
