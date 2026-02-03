package com.example.messmateapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.messmateapp.R;
import com.example.messmateapp.data.model.AuthResponse;
import com.example.messmateapp.data.remote.ApiClient;
import com.example.messmateapp.data.remote.ApiService;
import com.example.messmateapp.ui.home.HomeActivity;
import com.example.messmateapp.utils.SessionManager;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    /* ================= UI ================= */

    private EditText[] otpBoxes = new EditText[6];
    private LinearLayout otpLayout;

    private Button btnVerify;
    private ProgressBar progressBar;
    private TextView tvNumber, tvResend;
    private ImageView btnBack;


    /* ================= FIREBASE ================= */

    private FirebaseAuth mAuth;
    private String verificationId;
    private String mobile;

    private boolean isVerifying = false;


    /* ================= SESSION ================= */

    private SessionManager session;

    private CountDownTimer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        initViews();

        mAuth = FirebaseAuth.getInstance();
        session = new SessionManager(this);


        /* Get Data */
        verificationId = getIntent().getStringExtra("verificationId");
        mobile = getIntent().getStringExtra("mobile");


        if (mobile != null) {
            tvNumber.setText("+91 " + mobile);
        }


        /* If OTP not yet received */
        if (verificationId == null) {

            btnVerify.setEnabled(false);
            tvResend.setText("Sending OTP...");
        }


        startResendTimer();


        btnVerify.setOnClickListener(v -> verifyOtp());

        btnBack.setOnClickListener(v -> finish());

        tvResend.setOnClickListener(v -> resendOtp());
    }


    /* ================= INIT ================= */

    private void initViews() {

        btnVerify = findViewById(R.id.btnVerifyOtp);
        progressBar = findViewById(R.id.progressBar);

        tvNumber = findViewById(R.id.tvNumber);
        tvResend = findViewById(R.id.tvTimer);

        btnBack = findViewById(R.id.btnBack);

        otpLayout = findViewById(R.id.otpLayout);


        for (int i = 0; i < 6; i++) {

            otpBoxes[i] = (EditText) otpLayout.getChildAt(i);
        }

        setupOtpInputs();
    }


    /* ================= OTP MOVE ================= */

    private void setupOtpInputs() {

        for (int i = 0; i < otpBoxes.length; i++) {

            final int index = i;

            otpBoxes[i].addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(
                        CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(
                        CharSequence s, int start, int before, int count) {

                    if (s.length() == 1 && index < 5) {

                        otpBoxes[index + 1].requestFocus();
                    }

                    if (s.length() == 0 && index > 0) {

                        otpBoxes[index - 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }


    /* ================= GET OTP ================= */

    private String getOtp() {

        StringBuilder otp = new StringBuilder();

        for (EditText box : otpBoxes) {

            otp.append(box.getText().toString().trim());
        }

        return otp.toString();
    }


    /* ================= LOADER ================= */

    private void showLoader() {

        progressBar.setVisibility(View.VISIBLE);
        btnVerify.setEnabled(false);
    }


    private void hideLoader() {

        progressBar.setVisibility(View.GONE);
        btnVerify.setEnabled(true);
    }


    /* ================= VERIFY ================= */

    private void verifyOtp() {

        if (isVerifying) return;


        if (verificationId == null) {

            Toast.makeText(
                    this,
                    "Please wait, sending OTP...",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String code = getOtp();


        if (code.length() != 6) {

            Toast.makeText(
                    this,
                    "Enter valid OTP",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        isVerifying = true;

        showLoader();


        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, code);

        signIn(credential);
    }


    /* ================= AUTO VERIFY ================= */

    private void autoVerify(PhoneAuthCredential credential) {

        if (isVerifying) return;

        isVerifying = true;

        showLoader();

        signIn(credential);
    }


    /* ================= FIREBASE LOGIN ================= */

    private void signIn(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {

                            sendToBackend(user);
                        }

                    } else {

                        isVerifying = false;

                        hideLoader();

                        Toast.makeText(
                                this,
                                "Invalid OTP",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }


    /* ================= BACKEND ================= */

    private void sendToBackend(FirebaseUser user) {

        user.getIdToken(true)
                .addOnSuccessListener(result -> {

                    String firebaseToken = result.getToken();


                    ApiService api =
                            ApiClient.getClient().create(ApiService.class);


                    api.firebaseLogin("Bearer " + firebaseToken)
                            .enqueue(new Callback<AuthResponse>() {

                                @Override
                                public void onResponse(
                                        Call<AuthResponse> call,
                                        Response<AuthResponse> res) {

                                    hideLoader();


                                    if (res.isSuccessful()
                                            && res.body() != null
                                            && res.body().isSuccess()) {

                                        /* ✅ SAVE FULL LOGIN */
                                        session.saveLogin(
                                                res.body().getToken(),
                                                mobile
                                        );


                                        startActivity(
                                                new Intent(
                                                        OtpActivity.this,
                                                        HomeActivity.class
                                                )
                                        );

                                        finishAffinity();

                                    } else {

                                        isVerifying = false;

                                        Toast.makeText(
                                                OtpActivity.this,
                                                "Login Failed",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }

                                @Override
                                public void onFailure(
                                        Call<AuthResponse> call,
                                        Throwable t) {

                                    hideLoader();

                                    isVerifying = false;

                                    Toast.makeText(
                                            OtpActivity.this,
                                            t.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            });

                })
                .addOnFailureListener(e -> {

                    hideLoader();

                    isVerifying = false;

                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    /* ================= RESEND ================= */

    private void resendOtp() {

        if (mobile == null) return;


        tvResend.setEnabled(false);


        String phone = "+91" + mobile;


        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

        startResendTimer();
    }


    /* ================= CALLBACK ================= */

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(
                        PhoneAuthCredential credential) {

                    String code = credential.getSmsCode();


                    /* Auto Fill */
                    if (code != null && code.length() == 6) {

                        for (int i = 0; i < 6; i++) {

                            otpBoxes[i].setText(
                                    String.valueOf(code.charAt(i))
                            );
                        }
                    }


                    /* Auto Verify */
                    autoVerify(credential);
                }


                @Override
                public void onVerificationFailed(
                        FirebaseException e) {

                    Toast.makeText(
                            OtpActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                }


                @Override
                public void onCodeSent(
                        String id,
                        PhoneAuthProvider.ForceResendingToken token) {

                    verificationId = id;

                    btnVerify.setEnabled(true);

                    tvResend.setText("Resend OTP");
                }
            };


    /* ================= TIMER ================= */

    private void startResendTimer() {

        tvResend.setEnabled(false);


        timer = new CountDownTimer(30000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                tvResend.setText(
                        "Resend in " + (millisUntilFinished / 1000) + "s"
                );
            }

            @Override
            public void onFinish() {

                tvResend.setEnabled(true);

                tvResend.setText("Resend OTP");
            }

        }.start();
    }


    @Override
    protected void onDestroy() {

        if (timer != null) timer.cancel();

        super.onDestroy();
    }
}
