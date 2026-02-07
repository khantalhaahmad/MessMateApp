package com.example.messmateapp.ui.profile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.messmateapp.R;
import com.example.messmateapp.data.model.UserResponse;
import com.example.messmateapp.data.remote.ApiClient;
import com.example.messmateapp.data.remote.ApiService;
import com.example.messmateapp.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail, etDob, etGender, etPhone;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        loadProfile();
        setupClick();
    }

    /* ================= INIT ================= */

    private void initViews() {

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etDob = findViewById(R.id.etDob);
        etGender = findViewById(R.id.etGender);

        btnSave = findViewById(R.id.btnSaveProfile);
    }

    /* ================= LOAD PROFILE ================= */

    private void loadProfile() {

        ApiService api = ApiClient.getClient().create(ApiService.class);

        // ✅ Get Token
        SessionManager session = new SessionManager(this);
        String token = session.getToken();

        if (token == null) {

            Toast.makeText(
                    this,
                    "Login expired. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String authToken = "Bearer " + token;

        // ✅ API Call with Token
        api.getProfile(authToken).enqueue(new Callback<UserResponse>() {

            @Override
            public void onResponse(Call<UserResponse> call,
                                   Response<UserResponse> response) {

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().isSuccess()) {

                    UserResponse.User user = response.body().getUser();

                    etName.setText(user.getName());
                    etEmail.setText(user.getEmail());
                    etPhone.setText(user.getPhone());

                    // DOB null safety
                    if (user.getDob() != null) {
                        etDob.setText(user.getDob());
                    }

                    etGender.setText(user.getGender());

                } else {

                    Toast.makeText(
                            EditProfileActivity.this,
                            "Failed to load profile (" + response.code() + ") ❌",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {

                Toast.makeText(
                        EditProfileActivity.this,
                        "Network Error ❌",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    /* ================= CLICK ================= */

    private void setupClick() {

        btnSave.setOnClickListener(v -> saveProfile());
    }

    /* ================= SAVE ================= */

    private void saveProfile() {

        // ================= GET DATA =================

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Convert DOB to backend format
        String dob = convertDate(etDob.getText().toString().trim());

        // Backend only accepts: male / female / other
        String gender = etGender.getText().toString().toLowerCase().trim();


        // ================= VALIDATION =================

        if (name.isEmpty()) {
            etName.setError("Enter name");
            return;
        }

        if (gender.length() > 0 &&
                !(gender.equals("male")
                        || gender.equals("female")
                        || gender.equals("other"))) {

            etGender.setError("Enter: male / female / other");
            return;
        }


        // ================= TOKEN =================

        SessionManager session = new SessionManager(this);

        String token = session.getToken();

        if (token == null) {

            Toast.makeText(this,
                    "Login expired. Please login again.",
                    Toast.LENGTH_LONG).show();

            return;
        }

        String authToken = "Bearer " + token;


        // ================= REQUEST BODY =================

        Map<String, String> body = new HashMap<>();

        body.put("name", name);
        body.put("email", email);
        body.put("phone", phone);
        body.put("dob", dob);
        body.put("gender", gender);


        // ================= DEBUG =================

        Log.d("PROFILE_DEBUG", "====== Sending Profile Data ======");
        Log.d("PROFILE_DEBUG", "Token: " + authToken);
        Log.d("PROFILE_DEBUG", "Name: " + name);
        Log.d("PROFILE_DEBUG", "Email: " + email);
        Log.d("PROFILE_DEBUG", "Phone: " + phone);
        Log.d("PROFILE_DEBUG", "DOB: " + dob);
        Log.d("PROFILE_DEBUG", "Gender: " + gender);


        // ================= API =================

        ApiService api = ApiClient.getClient().create(ApiService.class);

        btnSave.setEnabled(false);


        api.updateProfile(authToken, body).enqueue(new Callback<UserResponse>() {

            @Override
            public void onResponse(Call<UserResponse> call,
                                   Response<UserResponse> response) {

                btnSave.setEnabled(true);

                Log.d("PROFILE_DEBUG", "HTTP Code: " + response.code());


                // ================= ERROR BODY DEBUG =================

                if (!response.isSuccessful()) {

                    try {

                        if (response.errorBody() != null) {

                            String error =
                                    response.errorBody().string();

                            Log.e("PROFILE_DEBUG", "Server Error: " + error);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


                // ================= SUCCESS =================

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().isSuccess()) {

                    Toast.makeText(
                            EditProfileActivity.this,
                            "Profile Updated ✅",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();

                } else {

                    Toast.makeText(
                            EditProfileActivity.this,
                            "Update Failed ❌ (" + response.code() + ")",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }


            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {

                btnSave.setEnabled(true);

                Log.e("PROFILE_DEBUG", "API Failed", t);

                Toast.makeText(
                        EditProfileActivity.this,
                        "Network Error ❌",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private String convertDate(String input) {

        try {

            SimpleDateFormat in =
                    new SimpleDateFormat("dd/MM/yyyy");

            SimpleDateFormat out =
                    new SimpleDateFormat("yyyy-MM-dd");

            Date date = in.parse(input);

            return out.format(date);

        } catch (Exception e) {

            Log.e("PROFILE_DEBUG", "DOB Parse Error", e);

            return "";
        }
    }
}
