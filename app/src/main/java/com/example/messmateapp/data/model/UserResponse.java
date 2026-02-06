package com.example.messmateapp.data.model;

public class UserResponse {

    private boolean success;
    private User user;


    public boolean isSuccess() {
        return success;
    }

    public User getUser() {
        return user;
    }


    // ================= INNER USER MODEL =================

    public static class User {

        private String id;
        private String name;
        private String phone;
        private String email;
        private String role;
        private String avatar;
        private boolean profileComplete;


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

        public boolean isProfileComplete() {
            return profileComplete;
        }
    }
}
