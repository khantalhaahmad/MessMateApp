package com.example.messmateapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "messmate_session";

    // Flags
    private static final String KEY_LOGIN = "is_logged_in";
    private static final String KEY_SAVED = "has_saved_account";

    // Data
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_MOBILE = "mobile_number";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;


    public SessionManager(Context context) {

        pref = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );

        editor = pref.edit();
    }


    /* ================= SAVE LOGIN ================= */

    // After successful OTP + backend login
    public void saveLogin(String token, String mobile) {

        editor.putBoolean(KEY_LOGIN, true);   // User is logged in
        editor.putBoolean(KEY_SAVED, true);   // Account is saved

        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_MOBILE, mobile);

        editor.apply();
    }


    /* ================= LOGOUT ================= */

    // Logout → Token remove → Saved account stays
    public void logout() {

        editor.putBoolean(KEY_LOGIN, false);  // Not logged in
        editor.remove(KEY_TOKEN);             // Remove token only

        editor.apply();
    }


    /* ================= REMOVE ACCOUNT ================= */

    // Remove account → Full reset
    public void removeAccount() {

        editor.clear();
        editor.apply();
    }


    /* ================= CHECK ================= */

    // Check login
    public boolean isLoggedIn() {

        return pref.getBoolean(KEY_LOGIN, false)
                && getToken() != null;
    }


    // Check saved account
    public boolean hasSavedAccount() {

        return pref.getBoolean(KEY_SAVED, false)
                && getMobile() != null;
    }


    /* ================= GET DATA ================= */

    public String getToken() {

        return pref.getString(KEY_TOKEN, null);
    }


    public String getMobile() {

        return pref.getString(KEY_MOBILE, null);
    }
}
