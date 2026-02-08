package com.example.messmateapp.data.model;

public class OrderDto {

    private String _id;
    private String mess_name;
    private double total_price;
    private String status;
    private String paymentMethod;
    private String createdAt;

    public String getId() {
        return _id;
    }

    public String getMessName() {
        return mess_name;
    }

    public double getTotalPrice() {
        return total_price;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
