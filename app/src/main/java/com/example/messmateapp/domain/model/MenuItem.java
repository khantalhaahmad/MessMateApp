package com.example.messmateapp.domain.model;

public class MenuItem {

    private final String id;
    private final String name;
    private final String description;
    private final double price;
    private final String image;

    // 🔥 VEG / NON-VEG FLAG (USED FOR FILTERING)
    private final boolean isVeg;

    public MenuItem(
            String id,
            String name,
            String description,
            double price,
            String image,
            boolean isVeg
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
        this.isVeg = isVeg;
    }

    // ✅ GETTERS
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    // ✅ USED IN FILTER (Veg / Non-Veg)
    public boolean isVeg() {
        return isVeg;
    }

    // ✅ OPTIONAL (HELPFUL)
    public boolean isNonVeg() {
        return !isVeg;
    }
}
