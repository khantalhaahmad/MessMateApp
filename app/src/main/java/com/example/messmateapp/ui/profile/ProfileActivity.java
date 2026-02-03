package com.example.messmateapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.messmateapp.R;
import com.example.messmateapp.ui.auth.LoginActivity;
import com.example.messmateapp.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    /* ================= UI ================= */

    private Button btnLogout;
    private ImageView btnBack;

    /* ================= SESSION ================= */

    private SessionManager session;

    /* ================= FIREBASE ================= */

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();

        session = new SessionManager(this);
        mAuth = FirebaseAuth.getInstance();


        // Back Button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }


        // Logout Button
        btnLogout.setOnClickListener(v -> logoutUser());
    }


    /* ================= INIT ================= */

    private void initViews() {

        btnLogout = findViewById(R.id.btnLogout);

        // Optional back button (if exists in layout)
        btnBack = findViewById(R.id.btnBack);
    }


    /* ================= LOGOUT ================= */

    private void logoutUser() {

        try {

            // Firebase Logout
            if (mAuth != null) {
                mAuth.signOut();
            }

            // ✅ Clear only TOKEN (Mobile stays saved)
            if (session != null) {
                session.logout();
            }

            Toast.makeText(
                    this,
                    "Logged out successfully",
                    Toast.LENGTH_SHORT
            ).show();


            // Go to Login Screen
            Intent intent = new Intent(
                    ProfileActivity.this,
                    LoginActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Logout failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT
            ).show();

            e.printStackTrace();
        }
    }


    /* ================= BACK PRESS ================= */

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
