package com.example.messmateapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpActivity extends AppCompatActivity {

    /* ================= UI ================= */

    private EditText[] otpBoxes = new EditText[6];
    private LinearLayout otpLayout;

    private Button btnVerify;
    private ProgressBar progressBar;

    private TextView tvNumber, tvResend, tvDidntGet, tvBackLogin;

    private ImageView btnBack;

    private FrameLayout errorContainer;


    /* ================= FIREBASE ================= */

    private FirebaseAuth mAuth;
    private String verificationId;
    private String mobile;

    private boolean isVerifying = false;


    /* ================= SESSION ================= */

    private SessionManager session;
    private CountDownTimer timer;


    /* ================= ACTIVITY ================= */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        initViews();

        mAuth = FirebaseAuth.getInstance();
        session = new SessionManager(this);


        // Get Data
        verificationId = getIntent().getStringExtra("verificationId");
        mobile = getIntent().getStringExtra("mobile");


        if (mobile != null) {
            tvNumber.setText("+91 " + mobile);
        }


        // If OTP not yet received
        if (verificationId == null) {

            btnVerify.setEnabled(false);
            tvResend.setText("Sending OTP...");
        }


        startResendTimer();

        // Default disable
        disableBackLogin();


        // Go Back Login
        tvBackLogin.setOnClickListener(v -> {

            if (!tvBackLogin.isEnabled()) return;

            Intent intent =
                    new Intent(OtpActivity.this, LoginActivity.class);

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(intent);
            finish();
        });


        // Back Icon
        btnBack.setOnClickListener(v -> finish());


        // Resend
        tvResend.setOnClickListener(v -> resendOtp());
    }


    /* ================= INIT ================= */

    private void initViews() {

        progressBar = findViewById(R.id.progressBar);

        tvNumber = findViewById(R.id.tvNumber);
        tvDidntGet = findViewById(R.id.tvDidntGet);
        tvResend = findViewById(R.id.tvResend);
        tvBackLogin = findViewById(R.id.tvBackLogin);

        btnBack = findViewById(R.id.btnBack);

        otpLayout = findViewById(R.id.otpLayout);
        errorContainer = findViewById(R.id.errorContainer);


        for (int i = 0; i < 6; i++) {

            otpBoxes[i] = (EditText) otpLayout.getChildAt(i);
        }

        setupOtpInputs();
    }


    /* ================= ENABLE / DISABLE BACK ================= */

    private void disableBackLogin() {

        tvBackLogin.setEnabled(false);
        tvBackLogin.setClickable(false);

        tvBackLogin.setTextColor(
                getResources().getColor(R.color.textHint)
        );
    }


    private void enableBackLogin() {

        tvBackLogin.setEnabled(true);
        tvBackLogin.setClickable(true);

        tvBackLogin.setTextColor(
                getResources().getColor(R.color.accent)
        );
    }


    /* ================= OTP INPUT ================= */

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

                    if (allOtpFilled() && !isVerifying) {
                        verifyOtp();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }


    private boolean allOtpFilled() {

        for (EditText box : otpBoxes) {

            if (box.getText().toString().trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }


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
    }

    private void hideLoader() {
        progressBar.setVisibility(View.GONE);
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

                        showWrongOtpEffect();
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
                                    }
                                }

                                @Override
                                public void onFailure(
                                        Call<AuthResponse> call,
                                        Throwable t) {

                                    hideLoader();
                                    isVerifying = false;
                                }
                            });

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


    /* ================= WRONG OTP EFFECT ================= */

    private void showWrongOtpEffect() {

        isVerifying = false;

        Animation shake =
                AnimationUtils.loadAnimation(this, R.anim.shake);

        otpLayout.startAnimation(shake);


        showOtpError("The OTP entered is invalid. Please try again");


        Animation fall =
                AnimationUtils.loadAnimation(this, R.anim.otp_fall);

        for (EditText box : otpBoxes) {

            box.startAnimation(fall);
            box.setText("");
        }


        otpBoxes[0].requestFocus();
    }


    private void showOtpError(String msg) {

        errorContainer.removeAllViews();

        View view = getLayoutInflater()
                .inflate(R.layout.snackbar_otp_error, null);

        TextView txt = view.findViewById(R.id.txtMsg);
        txt.setText(msg);

        errorContainer.addView(view);
        errorContainer.setVisibility(View.VISIBLE);

        errorContainer.postDelayed(() ->
                        errorContainer.setVisibility(View.GONE),
                2500);
    }


    /* ================= TIMER ================= */

    private void startResendTimer() {

        disableBackLogin();

        tvResend.setEnabled(false);
        tvResend.setClickable(false);

        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(30000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                int sec = (int) (millisUntilFinished / 1000);

                tvResend.setText("Resend SMS in " + sec + "s");
                tvResend.setTextColor(
                        getResources().getColor(R.color.textHint)
                );

                disableBackLogin();
            }

            @Override
            public void onFinish() {

                tvResend.setEnabled(true);
                tvResend.setClickable(true);

                tvResend.setText("Resend SMS");
                tvResend.setTextColor(
                        getResources().getColor(R.color.errorRed)
                );

                enableBackLogin();
            }

        }.start();
    }


    @Override
    protected void onDestroy() {

        if (timer != null) timer.cancel();

        super.onDestroy();
    }
}
