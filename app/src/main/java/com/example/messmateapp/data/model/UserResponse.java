package com.example.messmateapp.data.model;

public class UserResponse {

    private boolean success;
    private User user;


    /* ================= MAIN RESPONSE ================= */

    public boolean isSuccess() {
        return success;
    }

    public User getUser() {
        return user;
    }


    /* ================= INNER USER MODEL ================= */

    public static class User {

        private String id;
        private String name;
        private String phone;
        private String email;
        private String role;
        private String avatar;

        // ✅ NEW FIELDS
        private String dob;
        private String gender;

        private boolean profileComplete;


        /* ================= GETTERS ================= */

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }

        public String getAvatar() {
            return avatar;
        }

        // ✅ NEW GETTERS

        public String getDob() {
            return dob;
        }

        public String getGender() {
            return gender;
        }

        public boolean isProfileComplete() {
            return profileComplete;
        }
    }
}
