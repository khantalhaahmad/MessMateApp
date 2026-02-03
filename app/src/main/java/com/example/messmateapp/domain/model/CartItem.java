package com.example.messmateapp.domain.model;

public class CartItem {

    public static final int MAX_QTY = 5; // 🔒 HARD LIMIT

    private String id;
    private String name;
    private int price;
    private int quantity;
    private String image;
    private String type;
    private String category;

    public CartItem(String id, String name, int price, int quantity,
                    String image, String type, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.image = image;
        this.type = type;
        this.category = category;
    }

    public int getTotal() {
        return price * quantity;
    }

    public boolean canIncrease() {
        return quantity < MAX_QTY;
    }

    // getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getImage() { return image; }
    public String getType() { return type; }
    public String getCategory() { return category; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
