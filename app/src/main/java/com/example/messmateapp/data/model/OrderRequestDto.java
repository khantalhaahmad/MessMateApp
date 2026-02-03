package com.example.messmateapp.data.model;

import java.util.List;

public class OrderRequestDto {

    /* ================= ORDER ================= */

    public String mess_id;
    public String mess_name;

    public String paymentMethod; // Online / COD
    public int total_price;

    public List<CartItemDto> items;


    /* ================= ADDRESS (NEW) ================= */

    // Full readable address
    public String deliveryAddress;

    // Location
    public Double lat;
    public Double lng;


    /* ================= RECEIVER (OPTIONAL) ================= */

    public String receiverName;
    public String receiverPhone;


    /* ================= CART ================= */

    public static class CartItemDto {

        public String name;
        public int price;
        public int quantity;

        public String image;
        public String type;
        public String category;
    }
}
