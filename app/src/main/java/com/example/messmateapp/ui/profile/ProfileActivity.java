package com.example.messmateapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.messmateapp.R;
import com.example.messmateapp.data.remote.ApiClient;
import com.example.messmateapp.data.remote.ApiService;
import com.example.messmateapp.data.model.UserResponse;
import com.example.messmateapp.ui.address.AddressListActivity;
import com.example.messmateapp.ui.auth.LoginActivity;
import com.example.messmateapp.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.LinearLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    /* ================= UI ================= */

    private TextView tvName, tvAvatar, tvEditText;

    private LinearLayout btnEdit, btnAddress;

    private Switch switchDark, switchNotify;
    private Button btnLogout;
    private ImageView btnBack;

    /* ================= SESSION ================= */

    private SessionManager session;

    /* ================= API ================= */

    private ApiService api;

    /* ================= FIREBASE ================= */

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();

        session = new SessionManager(this);
        api = ApiClient.getClient().create(ApiService.class);
        mAuth = FirebaseAuth.getInstance();

        loadProfile();
        loadSettings();

        // Back
        btnBack.setOnClickListener(v -> onBackPressed());

        // Edit Profile
        tvEditText.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Edit Profile (Coming Soon)",
                        Toast.LENGTH_SHORT
                ).show()
        );

        // Address Book
        btnAddress.setOnClickListener(v ->
                startActivity(new Intent(this,
                        com.example.messmateapp.ui.address.AddressZomatoActivity.class))
        );


        // Dark Mode
        switchDark.setOnCheckedChangeListener((b, isOn) -> {

            session.setDarkMode(isOn);

            AppCompatDelegate.setDefaultNightMode(
                    isOn
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        // Notification
        switchNotify.setOnCheckedChangeListener((b, isOn) ->
                session.setNotification(isOn)
        );

        // Logout
        btnLogout.setOnClickListener(v -> logoutUser());
    }


    /* ================= INIT ================= */

    private void initViews() {

        btnBack = findViewById(R.id.btnBack);

        tvName = findViewById(R.id.tvName);
        tvEditText = findViewById(R.id.tvEditText);

        btnAddress = findViewById(R.id.layoutAddress);

        switchDark = findViewById(R.id.switchDark);
        switchNotify = findViewById(R.id.switchNotify);

        btnLogout = findViewById(R.id.btnLogout);
    }



    /* ================= LOAD PROFILE ================= */

    private void loadProfile() {

        api.getProfile().enqueue(new Callback<UserResponse>() {

            @Override
            public void onResponse(Call<UserResponse> call,
                                   Response<UserResponse> response) {

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getUser() != null) {

                    UserResponse.User user = response.body().getUser();

                    // Get Name
                    String name = user.getName();

                    // Show Name Only
                    tvName.setText(name != null ? name : "User");

                    // Set Avatar (First Letter)
                    TextView tvAvatar = findViewById(R.id.tvAvatar);

                    if (tvAvatar != null && name != null && !name.isEmpty()) {

                        tvAvatar.setText(
                                name.substring(0, 1).toUpperCase()
                        );
                    }

                } else {

                    Toast.makeText(
                            ProfileActivity.this,
                            "Failed to load profile",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {

                Toast.makeText(
                        ProfileActivity.this,
                        "Server error",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }


    /* ================= LOAD SETTINGS ================= */

    private void loadSettings() {

        switchDark.setChecked(session.isDark());
        switchNotify.setChecked(session.isNotify());
    }


    /* ================= LOGOUT ================= */

    private void logoutUser() {

        try {

            if (mAuth != null) {
                mAuth.signOut();
            }

            if (session != null) {
                session.logout();
            }

            Toast.makeText(
                    this,
                    "Logged out successfully",
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent = new Intent(this, LoginActivity.class);

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Logout failed",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    /* ================= BACK ================= */

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
