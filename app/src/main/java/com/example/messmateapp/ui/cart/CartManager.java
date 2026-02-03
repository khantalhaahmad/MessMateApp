package com.example.messmateapp.ui.cart;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.messmateapp.domain.model.CartItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartManager {

    /* ==========================
       🔒 MULTI CART (Persistent)
       ========================== */

    private static final Map<String, List<CartItem>> cartMap =
            new HashMap<>();

    private static String currentRestaurantId = "";


    /* ==========================
       💾 PREF
       ========================== */

    private static final String PREF_NAME = "CART_PREF";
    private static final String KEY_CART = "CART_DATA";
    private static final String KEY_LAST_RESTAURANT = "LAST_RESTAURANT";


    // 🔥 HARD LIMIT
    public static final int MAX_QTY_PER_ITEM = 5;


    /* ==========================
       🏪 SET RESTAURANT
       ========================== */

    public static synchronized void setRestaurant(String resId, Context ctx) {

        if (resId == null || resId.isEmpty() || ctx == null) return;

        // Load all carts first
        loadFromStorage(ctx);

        currentRestaurantId = resId;

        // Ensure cart exists
        if (!cartMap.containsKey(resId)) {
            cartMap.put(resId, new ArrayList<>());
        }

        saveLastRestaurant(ctx);
    }

    /* ==========================
       📌 LAST RESTAURANT
       ========================== */

    private static void saveLastRestaurant(Context ctx) {

        if (ctx == null || currentRestaurantId.isEmpty()) return;

        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LAST_RESTAURANT, currentRestaurantId)
                .apply();
    }


    public static String getLastRestaurant(Context ctx) {

        if (ctx == null) return "";

        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LAST_RESTAURANT, "");
    }


    /* ==========================
       📦 CURRENT CART
       ========================== */

    private static List<CartItem> getCurrentCart() {

        if (currentRestaurantId == null || currentRestaurantId.isEmpty()) {
            return new ArrayList<>();
        }

        if (!cartMap.containsKey(currentRestaurantId)) {
            cartMap.put(currentRestaurantId, new ArrayList<>());
        }

        return cartMap.get(currentRestaurantId);
    }


    /* ==========================
       💾 SAVE
       ========================== */

    private static void save(Context ctx) {

        if (ctx == null) return;

        SharedPreferences pref =
                ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        Gson gson = new Gson();

        String json = gson.toJson(cartMap);

        pref.edit()
                .putString(KEY_CART, json)
                .apply();
    }


    /* ==========================
       📥 LOAD
       ========================== */

    // Called from Splash / App Start
    public static synchronized void loadFromStorage(Context ctx) {

        if (ctx == null) return;

        SharedPreferences pref =
                ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String json = pref.getString(KEY_CART, null);

        if (json == null || json.isEmpty()) return;

        try {

            Gson gson = new Gson();

            Type type =
                    new TypeToken<Map<String, List<CartItem>>>() {}.getType();

            Map<String, List<CartItem>> saved =
                    gson.fromJson(json, type);

            if (saved != null && !saved.isEmpty()) {

                cartMap.clear();
                cartMap.putAll(saved);

                // ❌ yahan currentRestaurantId set mat karo
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ==========================
       ➕ ADD
       ========================== */

    public static synchronized boolean addItem(
            CartItem item,
            Context ctx
    ) {

        if (item == null || item.getId() == null) return false;

        List<CartItem> cart = getCurrentCart();

        for (CartItem c : cart) {

            if (c.getId().equals(item.getId())) {

                if (c.getQuantity() >= MAX_QTY_PER_ITEM) {
                    return false;
                }

                c.setQuantity(c.getQuantity() + 1);

                save(ctx);
                saveLastRestaurant(ctx);

                return true;
            }
        }

        item.setQuantity(1);
        cart.add(item);

        save(ctx);
        saveLastRestaurant(ctx);

        return true;
    }


    /* ==========================
       ➕ INCREASE
       ========================== */

    public static synchronized boolean increase(
            String itemId,
            Context ctx
    ) {

        if (itemId == null) return false;

        for (CartItem c : getCurrentCart()) {

            if (c.getId().equals(itemId)) {

                if (c.getQuantity() >= MAX_QTY_PER_ITEM) {
                    return false;
                }

                c.setQuantity(c.getQuantity() + 1);

                save(ctx);
                saveLastRestaurant(ctx);

                return true;
            }
        }

        return false;
    }


    /* ==========================
       ➖ DECREASE
       ========================== */

    public static synchronized boolean decrease(
            String itemId,
            Context ctx
    ) {

        if (itemId == null) return false;

        List<CartItem> cart = getCurrentCart();

        for (int i = 0; i < cart.size(); i++) {

            CartItem c = cart.get(i);

            if (c.getId().equals(itemId)) {

                if (c.getQuantity() > 1) {

                    c.setQuantity(c.getQuantity() - 1);

                } else {

                    cart.remove(i);
                }

                save(ctx);

                return true;
            }
        }

        return false;
    }


    /* ==========================
       🔢 QTY
       ========================== */

    public static synchronized int getItemQty(String itemId) {

        if (itemId == null) return 0;

        for (CartItem c : getCurrentCart()) {

            if (c.getId().equals(itemId)) {
                return c.getQuantity();
            }
        }

        return 0;
    }


    /* ==========================
       🧮 TOTAL ITEMS
       ========================== */

    public static synchronized int getTotalItems() {

        int count = 0;

        for (CartItem c : getCurrentCart()) {
            count += c.getQuantity();
        }

        return count;
    }


    /* ==========================
       💰 TOTAL AMOUNT
       ========================== */

    public static synchronized int getTotalAmount() {

        int total = 0;

        for (CartItem c : getCurrentCart()) {
            total += c.getPrice() * c.getQuantity();
        }

        return total;
    }


    /* ==========================
       📦 ITEMS
       ========================== */

    public static synchronized List<CartItem> getItems() {

        return new ArrayList<>(getCurrentCart());
    }


    /* ==========================
       🧹 CLEAR (ON ORDER / CROSS)
       ========================== */

    public static synchronized void clear(Context ctx) {

        if (currentRestaurantId != null && !currentRestaurantId.isEmpty()) {

            cartMap.remove(currentRestaurantId);
        }

        save(ctx);

        // Remove last restaurant
        if (ctx != null) {

            ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .remove(KEY_LAST_RESTAURANT)
                    .apply();
        }

        currentRestaurantId = "";
    }


    /* ==========================
   📌 GET LAST ACTIVE RESTAURANT
   ========================== */

    public static synchronized String getLastRestaurantId(Context ctx) {

        if (ctx == null) return "";

        // First try from pref
        String last =
                ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                        .getString(KEY_LAST_RESTAURANT, "");

        if (last != null && !last.isEmpty()) {
            return last;
        }

        // Fallback from cartMap
        for (String key : cartMap.keySet()) {

            List<CartItem> list = cartMap.get(key);

            if (list != null && !list.isEmpty()) {
                return key;
            }
        }

        return "";
    }


/* ==========================
   🔢 TOTAL ITEMS (BY RESTAURANT)
   ========================== */

    public static synchronized int getTotalItemsFor(String resId) {

        if (resId == null || resId.isEmpty()) return 0;

        List<CartItem> list = cartMap.get(resId);

        if (list == null) return 0;

        int count = 0;

        for (CartItem c : list) {
            count += c.getQuantity();
        }

        return count;
    }


/* ==========================
   🧹 CLEAR BY RESTAURANT
   ========================== */

    public static synchronized void clearByRestaurant(
            String resId,
            Context ctx
    ) {

        if (resId == null || resId.isEmpty()) return;

        if (cartMap.containsKey(resId)) {

            cartMap.get(resId).clear();

            save(ctx);
        }

        // Remove last restaurant
        if (ctx != null) {

            ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .remove(KEY_LAST_RESTAURANT)
                    .apply();
        }

        if (resId.equals(currentRestaurantId)) {
            currentRestaurantId = "";
        }

    }

    /* ==========================
   📋 GET ALL ACTIVE CARTS
   ========================== */

    public static synchronized List<String> getAllActiveRestaurants() {

        List<String> list = new ArrayList<>();

        for (Map.Entry<String, List<CartItem>> entry : cartMap.entrySet()) {

            if (entry.getValue() != null && !entry.getValue().isEmpty()) {

                list.add(entry.getKey());
            }
        }

        return list;
    }

    /* ==========================
   🔢 TOTAL ACTIVE CARTS
   ========================== */

    public static synchronized int getActiveCartCount() {

        int count = 0;

        for (List<CartItem> list : cartMap.values()) {

            if (list != null && !list.isEmpty()) {
                count++;
            }
        }

        return count;
    }


    /* ==========================
       🔄 SYNC
       ========================== */

    public static synchronized void sync(Context ctx) {

        List<CartItem> cart = getCurrentCart();

        if (cart.isEmpty()) return;

        cart.removeIf(c -> c.getQuantity() <= 0);

        save(ctx);
    }
}
