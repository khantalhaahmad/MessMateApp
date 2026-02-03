package com.example.messmateapp.domain.model;

import java.util.List;

public class Restaurant {

    private final String id;
    private final String name;
    private final String location;
    private final String banner;
    private final double rating;
    private final List<MenuItem> menuItems;

    public Restaurant(
            String id,
            String name,
            String location,
            String banner,
            double rating,
            List<MenuItem> menuItems
    ) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.banner = banner;
        this.rating = rating;
        this.menuItems = menuItems;
    }

    // ✅ REQUIRED GETTERS (VERY IMPORTANT)
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getBanner() {
        return banner;
    }

    public double getRating() {
        return rating;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }
}
