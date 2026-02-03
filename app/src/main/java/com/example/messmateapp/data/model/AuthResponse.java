package com.example.messmateapp.data.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("token")
    private String token;

    @SerializedName("user")
    private User user;


    /* ================= GETTERS ================= */

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }


    /* ================= USER MODEL ================= */

    public static class User {

        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("email")
        private String email;

        @SerializedName("phone")
        private String phone;

        @SerializedName("role")
        private String role;

        @SerializedName("avatar")
        private String avatar;


        /* ============ GETTERS ============ */

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public String getRole() {
            return role;
        }

        public String getAvatar() {
            return avatar;
        }
    }
}
