package com.example.messmateapp.ui.splash;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.messmateapp.R;
import com.example.messmateapp.ui.auth.LoginActivity;
import com.example.messmateapp.ui.cart.CartManager;
import com.example.messmateapp.ui.home.HomeActivity;
import com.example.messmateapp.utils.SessionManager;
import com.google.firebase.messaging.FirebaseMessaging;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 2000;
    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SessionManager session = new SessionManager(this);

        // ✅ Load saved cart
        CartManager.loadFromStorage(this);

        // ✅ Ask notification permission (Android 13+)
        requestNotificationPermission();

        // ✅ Get FCM Token
        fetchFcmToken(session);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            if (session.isLoggedIn()) {
                startActivity(new Intent(
                        SplashActivity.this,
                        HomeActivity.class
                ));
            } else {
                startActivity(new Intent(
                        SplashActivity.this,
                        LoginActivity.class
                ));
            }

            finish();

        }, SPLASH_TIME);
    }

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }

    private void fetchFcmToken(SessionManager session) {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        return;
                    }

                    String token = task.getResult();

                    // 🔥 Save locally
                    session.saveFcmToken(token);

                    // 🔥 TODO: Send to backend if logged in
                    if (session.isLoggedIn()) {
                        // call your API here to save token in backend
                        // example: ApiClient.updateFcmToken(token);
                    }
                });
    }
}
