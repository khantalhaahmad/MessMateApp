package com.example.messmateapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RecommendationResponse {

    public boolean success;
    public String message;
    public List<RecommendationItem> data;


    // =============================
    // Single Recommendation Item
    // =============================
    public static class RecommendationItem {

        public String name;

        public String image;   // 🍔 Food image

        // 🏠 Restaurant banner image (FROM BACKEND)
        @SerializedName("mess_image")
        public String messImage;

        public int price;

        public double rating;

        public String description;

        public String type;        // veg / non-veg

        public String category;    // main-course etc

        public String mess_name;

        public String mess_id;
    }
}
