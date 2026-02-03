package com.example.messmateapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.messmateapp.ui.splash.SplashActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔥 Redirect to Splash (single source of truth)
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }
}
