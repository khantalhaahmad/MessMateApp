package com.example.messmateapp.data.model;

import java.util.List;

public class OrderDto {

    private String _id;

    // Backup name (old orders)
    private String mess_name;

    // ✅ New populated mess
    private MessRef mess_ref;

    private double total_price;
    private String status;
    private String paymentMethod;
    private String createdAt;

    // ✅ Items (for quantity + name)
    private List<ItemDto> items;

    private String mess_id;   // ✅ ADD THIS

    /* ================= GETTERS ================= */

    public String getId() {
        return _id;
    }

    // Prefer populated name
    public String getMessName() {

        if (mess_ref != null && mess_ref.getName() != null)
            return mess_ref.getName();

        return mess_name; // fallback
    }

    // ✅ Get banner image (from backend)
    public String getMessImage() {

        if (mess_ref != null)
            return mess_ref.getBanner();

        return null;
    }

    public double getTotalPrice() {
        return total_price;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public List<ItemDto> getItems() {
        return items;
    }


    /* ================= INNER CLASSES ================= */

    // ✅ For populate (matches backend: name + banner)
    public static class MessRef {

        private String name;
        private String banner;

        public String getName() {
            return name;
        }

        public String getBanner() {
            return banner;
        }
    }
    // ✅ For Reorder (from backend mess_id)
    public String getRestaurantId() {

        if (mess_id != null && !mess_id.isEmpty())
            return mess_id;

        return "";
    }

    // ✅ For items
    public static class ItemDto {

        private String name;
        private int quantity;

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
