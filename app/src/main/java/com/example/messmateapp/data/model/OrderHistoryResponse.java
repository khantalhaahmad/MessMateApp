package com.example.messmateapp.data.model;

import java.util.List;

public class OrderHistoryResponse {

    private boolean success;
    private List<OrderDto> orders;

    public boolean isSuccess() {
        return success;
    }

    public List<OrderDto> getOrders() {
        return orders;
    }
}
