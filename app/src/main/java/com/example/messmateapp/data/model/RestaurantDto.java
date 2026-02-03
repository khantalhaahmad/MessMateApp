package com.example.messmateapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RestaurantDto {

    @SerializedName("_id")
    private String id;

    private String name;
    private String location;
    private String banner;
    private double rating;

    // 🔥 NESTED MENU OBJECT
    @SerializedName("menu")
    private MenuDto menu;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getBanner() { return banner; }
    public double getRating() { return rating; }
    public MenuDto getMenu() { return menu; }

    // 🔹 MENU DTO
    public static class MenuDto {
        @SerializedName("items")
        private List<MenuItemDto> items;

        public List<MenuItemDto> getItems() {
            return items;
        }
    }
}
