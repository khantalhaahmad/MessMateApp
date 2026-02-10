package com.example.messmateapp.domain.model;

public class CartItem {

    public static final int MAX_QTY = 5; // 🔒 HARD LIMIT


    // ======================
    // 📦 ITEM INFO
    // ======================

    private String id;
    private String name;

    // Old price (from order history)
    private int price;

    // Latest price (from server)
    private int latestPrice;

    private int quantity;
    private String image;
    private String type;
    private String category;


    // ======================
    // 🏪 RESTAURANT INFO
    // ======================

    private String restaurantId;
    private String restaurantName;


    // ======================
    // 🔁 REORDER FLAGS
    // ======================

    private boolean available = true;
    private boolean priceUpdated = false;


    // ======================
    // ✅ CONSTRUCTOR
    // ======================

    public CartItem(
            String id,
            String name,
            int price,
            int latestPrice,
            int quantity,
            String image,
            String type,
            String category,
            String restaurantId,
            String restaurantName,
            boolean available,
            boolean priceUpdated
    ) {

        this.id = id;
        this.name = name;

        this.price = price;
        this.latestPrice = latestPrice;

        this.quantity = quantity;
        this.image = image;
        this.type = type;
        this.category = category;

        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;

        this.available = available;
        this.priceUpdated = priceUpdated;
    }


    // ======================
    // 🔢 HELPERS
    // ======================

    public int getTotal() {
        return getEffectivePrice() * quantity;
    }

    public boolean canIncrease() {
        return quantity < MAX_QTY;
    }


    // ======================
    // 💰 PRICE LOGIC
    // ======================

    // Use latest price if updated
    public int getEffectivePrice() {

        if (priceUpdated && latestPrice > 0) {
            return latestPrice;
        }

        return price;
    }


    // ======================
    // 📌 GETTERS
    // ======================

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getLatestPrice() {
        return latestPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getImage() {
        return image;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }


    // Restaurant Getters
    public String getRestaurantId() {
        return restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }


    // ======================
    // 🔁 REORDER GETTERS
    // ======================

    public boolean isAvailable() {
        return available;
    }

    public boolean isPriceUpdated() {
        return priceUpdated;
    }


    // ======================
    // ✏️ SETTERS
    // ======================

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setPriceUpdated(boolean priceUpdated) {
        this.priceUpdated = priceUpdated;
    }

    public void setLatestPrice(int latestPrice) {
        this.latestPrice = latestPrice;
    }
}
