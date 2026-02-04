package com.example.messmateapp.data.model;

public class CreateOrderResponse {

    public boolean success;
    public Order order;
    public String key;

    public static class Order {
        public String id;
        public int amount;
        public String currency;
    }
}
