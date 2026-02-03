package com.example.messmateapp.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.messmateapp.R;
import com.example.messmateapp.ui.auth.LoginActivity;
import com.example.messmateapp.ui.cart.CartManager;   // ✅ ADD
import com.example.messmateapp.ui.home.HomeActivity;
import com.example.messmateapp.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 2000; // 2 sec

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SessionManager session = new SessionManager(this);

        // 🔥 Load saved cart on app start (IMPORTANT)
        CartManager.loadFromStorage(this);

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
}
