package com.example.messmateapp.data.model;

import java.util.List;

// ✅ ADD THIS
import com.example.messmateapp.data.model.AddressDto;

public class OrderRequestDto {

    /* ================= ORDER ================= */

    public String mess_id;
    public String mess_name;

    public String paymentMethod; // Online / COD
    public int total_price;

    public List<CartItemDto> items;


    /* ================= ADDRESS ================= */

    // ✅ Send full structured address (not string)
    public AddressDto deliveryAddress;


    /* ================= RECEIVER ================= */

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
