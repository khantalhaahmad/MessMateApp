package com.example.messmateapp.data.model;

import com.google.gson.annotations.SerializedName;

public class MenuItemDto {

    @SerializedName("_id")
    private String id;

    private String name;
    private String description;
    private double price;
    private String image;

    @SerializedName("isVeg")
    private boolean isVeg;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImage() { return image; }
    public boolean isVeg() { return isVeg; }
}
