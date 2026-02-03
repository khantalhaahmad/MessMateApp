package com.example.messmateapp.data.model;

import java.util.List;

public class BannerDto {

    public static class Response {
        public boolean success;
        public List<Item> data;
    }

    public static class Item {
        public String image; // 🔥 FULL URL from backend (Cloudinary / localhost)
        public String name;
    }
}
