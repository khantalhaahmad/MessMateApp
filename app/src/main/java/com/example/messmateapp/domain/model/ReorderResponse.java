package com.example.messmateapp.domain.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReorderResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("restaurant")
    private Restaurant restaurant;

    // ⚠️ VERY IMPORTANT (Server sends "cartItems")
    @SerializedName("cartItems")
    private List<CartItem> cartItems;


    // ================= GETTERS =================

    public boolean isSuccess() {
        return success;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }


    // ================= RESTAURANT =================

    public static class Restaurant {

        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("banner")
        private String banner;


        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getBanner() {
            return banner;
        }
    }
}
