package com.example.messmateapp.data.model;

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

        public String image;

        public int price;

        public double rating;

        public String description;

        public String type;        // veg / non-veg

        public String category;    // main-course etc

        public String mess_name;

        public String mess_id;
    }
}
