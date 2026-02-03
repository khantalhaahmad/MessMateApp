package com.example.messmateapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.messmateapp.R;
import com.example.messmateapp.ui.home.HomeActivity;
import com.example.messmateapp.utils.SessionManager;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    /* ================= NORMAL LOGIN ================= */

    private EditText etPhone;
    private Button btnSendOtp;
    private View layoutNormalLogin;


    /* ================= BUTTON LOADER ================= */

    private ProgressBar btnLoader;


    /* ================= SAVED ACCOUNT ================= */

    private CardView cardSavedUser;
    private TextView tvAvatar, tvUserName, tvUserMobile;
    private ImageView btnMore;
    private ProgressBar savedLoader;


    /* ================= FIREBASE ================= */

    private FirebaseAuth mAuth;


    /* ================= SESSION ================= */

    private SessionManager session;

    private final String COUNTRY_CODE = "+91";


    /* Prevent double OTP */
    private boolean otpLaunched = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        session = new SessionManager(this);


        /* ================= AUTO REDIRECT ================= */

        // Case 1: Still logged in → HOME
        if (session.isLoggedIn()) {

            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }


        setContentView(R.layout.activity_login);

        initViews();

        mAuth = FirebaseAuth.getInstance();


        // Case 2: Logged out but saved account
        if (session.hasSavedAccount()) {

            setupSavedAccount();
        } else {

            showNormalLogin();
        }


        btnSendOtp.setOnClickListener(v -> {

            if (!btnSendOtp.isEnabled()) return;

            sendOtp();
        });
    }


    /* ================= INIT ================= */

    private void initViews() {

        etPhone = findViewById(R.id.etPhone);
        btnSendOtp = findViewById(R.id.btnSendOtp);

        btnLoader = findViewById(R.id.btnLoader);

        layoutNormalLogin = findViewById(R.id.layoutNormalLogin);

        cardSavedUser = findViewById(R.id.cardSavedUser);
        tvAvatar = findViewById(R.id.tvAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserMobile = findViewById(R.id.tvUserMobile);

        btnMore = findViewById(R.id.btnMore);
        savedLoader = findViewById(R.id.savedLoader);
    }


    /* ================= SAVED ACCOUNT ================= */

    private void setupSavedAccount() {

        String mobile = session.getMobile();


        if (mobile == null || mobile.length() != 10) {

            showNormalLogin();
            return;
        }


        cardSavedUser.setVisibility(View.VISIBLE);
        layoutNormalLogin.setVisibility(View.GONE);


        String masked =
                mobile.substring(0, 2) +
                        "XXXX" +
                        mobile.substring(6);


        tvAvatar.setText(
                String.valueOf(mobile.charAt(0)).toUpperCase()
        );

        tvUserName.setText("Talha");

        tvUserMobile.setText("+91 " + masked);


        /* Click → Loader → Home */
        cardSavedUser.setOnClickListener(v -> {

            cardSavedUser.setEnabled(false);

            btnMore.setVisibility(View.GONE);
            savedLoader.setVisibility(View.VISIBLE);


            new Handler().postDelayed(() -> {

                startActivity(
                        new Intent(LoginActivity.this, HomeActivity.class)
                );

                finish();

            }, 1500);
        });


        btnMore.setOnClickListener(v -> showAccountMenu());
    }


    /* ================= MENU ================= */

    private void showAccountMenu() {

        android.widget.PopupMenu menu =
                new android.widget.PopupMenu(this, btnMore);

        menu.getMenu().add("Remove account");


        menu.setOnMenuItemClickListener(item -> {

            if ("Remove account".contentEquals(item.getTitle())) {

                session.removeAccount();

                otpLaunched = false;


                btnMore.setVisibility(View.VISIBLE);
                savedLoader.setVisibility(View.GONE);
                cardSavedUser.setEnabled(true);


                showNormalLogin();


                Toast.makeText(
                        this,
                        "Account removed",
                        Toast.LENGTH_SHORT
                ).show();
            }

            return true;
        });

        menu.show();
    }


    /* ================= NORMAL LOGIN ================= */

    private void showNormalLogin() {

        cardSavedUser.setVisibility(View.GONE);

        layoutNormalLogin.setVisibility(View.VISIBLE);

        etPhone.setText("");
    }


    /* ================= BUTTON LOADER ================= */

    private void showButtonLoader() {

        btnSendOtp.setEnabled(false);
        btnSendOtp.setText("");

        btnLoader.setVisibility(View.VISIBLE);
    }


    private void hideButtonLoader() {

        btnSendOtp.setEnabled(true);
        btnSendOtp.setText("Continue");

        btnLoader.setVisibility(View.GONE);
    }


    /* ================= SEND OTP ================= */

    private void sendOtp() {

        otpLaunched = false;


        String number = etPhone.getText().toString().trim();


        if (number.length() != 10) {

            Toast.makeText(
                    this,
                    "Enter valid 10 digit number",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        showButtonLoader();


        String phone = COUNTRY_CODE + number;


        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }


    /* ================= FIREBASE CALLBACK ================= */

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(
                        @NonNull PhoneAuthCredential credential) {

                    // Don't auto-login
                }


                @Override
                public void onVerificationFailed(
                        @NonNull FirebaseException e) {

                    hideButtonLoader();

                    Toast.makeText(
                            LoginActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }


                @Override
                public void onCodeSent(
                        @NonNull String id,
                        @NonNull PhoneAuthProvider.ForceResendingToken token) {

                    hideButtonLoader();


                    if (otpLaunched) return;

                    otpLaunched = true;


                    Intent intent =
                            new Intent(LoginActivity.this, OtpActivity.class);

                    intent.putExtra("verificationId", id);
                    intent.putExtra("mobile", etPhone.getText().toString().trim());

                    startActivity(intent);
                }
            };
}
