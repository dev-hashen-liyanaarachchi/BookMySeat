package com.hash.bookmyseat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hash.bookmyseat.R;
import com.hash.bookmyseat.model.ApiService;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private TextView tvEmail, tvTimer, tvResend;
    private MaterialButton btnVerify;
    private String email, firstName, lastName, birth, password;
    private FirebaseAuth mAuth;
    private CountDownTimer countDownTimer;
    private static final long TIMER_DURATION = 60000; // 60 seconds
    private boolean canResend = false;
    private static final String BASE_URL = "http://192.168.8.190:8080/"; // Change to your IP
    private static final String TAG = "OTP_VERIFICATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_otp_verification);

        mAuth = FirebaseAuth.getInstance();

        // Get data from intent
        email = getIntent().getStringExtra("email");
        firstName = getIntent().getStringExtra("firstName");
        lastName = getIntent().getStringExtra("lastName");
        birth = getIntent().getStringExtra("birth");
        password = getIntent().getStringExtra("password");

        Log.d(TAG, "Received data - Email: " + email + ", FirstName: " + firstName);

        // Validate required data
        if (email == null || password == null) {
            Toast.makeText(this, "Error: Missing registration data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupOtpInputs();
        setClickListeners();
        startTimer();
    }

    private void initViews() {
        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);
        etOtp5 = findViewById(R.id.etOtp5);
        etOtp6 = findViewById(R.id.etOtp6);
        tvEmail = findViewById(R.id.tvEmail);
        tvTimer = findViewById(R.id.tvTimer);
        tvResend = findViewById(R.id.tvResend);
        btnVerify = findViewById(R.id.btnVerify);

        tvEmail.setText("OTP sent to " + email);
    }

    private void setupOtpInputs() {
        etOtp1.addTextChangedListener(new OtpTextWatcher(etOtp1, etOtp2));
        etOtp2.addTextChangedListener(new OtpTextWatcher(etOtp2, etOtp3));
        etOtp3.addTextChangedListener(new OtpTextWatcher(etOtp3, etOtp4));
        etOtp4.addTextChangedListener(new OtpTextWatcher(etOtp4, etOtp5));
        etOtp5.addTextChangedListener(new OtpTextWatcher(etOtp5, etOtp6));
        etOtp6.addTextChangedListener(new OtpTextWatcher(etOtp6, null));
    }

    private class OtpTextWatcher implements TextWatcher {
        private EditText current, next;

        public OtpTextWatcher(EditText current, EditText next) {
            this.current = current;
            this.next = next;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1 && next != null) {
                next.requestFocus();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    private void setClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnVerify.setOnClickListener(v -> verifyOTP());

        tvResend.setOnClickListener(v -> {
            if (canResend) {
                resendOTP();
            }
        });
    }

    private void startTimer() {
        tvResend.setEnabled(false);
        tvResend.setAlpha(0.5f);
        canResend = false;

        countDownTimer = new CountDownTimer(TIMER_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText("Resend in " + seconds + "s");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("");
                tvResend.setEnabled(true);
                tvResend.setAlpha(1f);
                canResend = true;
            }
        }.start();
    }

    private void verifyOTP() {
        // Get OTP from inputs
        String otp = etOtp1.getText().toString() +
                etOtp2.getText().toString() +
                etOtp3.getText().toString() +
                etOtp4.getText().toString() +
                etOtp5.getText().toString() +
                etOtp6.getText().toString();

        if (otp.length() < 6) {
            Toast.makeText(this, "Please enter complete OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Verifying OTP: " + otp + " for email: " + email);

        // Disable button and show loading
        btnVerify.setEnabled(false);
        btnVerify.setText("Verifying...");

        // Verify OTP with backend
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Map<String, Object> verifyRequest = new HashMap<>();
        verifyRequest.put("email", email);
        verifyRequest.put("otp", otp);

        apiService.verifyOtp(verifyRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Verify OTP response code: " + response.code());

                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ OTP verified successfully on server");

                    // OTP verified successfully, now register user in Firebase
                    registerUserInFirebase();

                } else {
                    Log.e(TAG, "❌ OTP verification failed on server. Code: " + response.code());
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body");
                    }

                    // Invalid OTP
                    handleInvalidOtp();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "❌ Network error during OTP verification: " + t.getMessage());

                // Network error
                btnVerify.setEnabled(true);
                btnVerify.setText("Verify");
                Toast.makeText(OtpVerificationActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleInvalidOtp() {
        btnVerify.setEnabled(true);
        btnVerify.setText("Verify");

        Toast.makeText(OtpVerificationActivity.this,
                "❌ Invalid OTP. Please try again.",
                Toast.LENGTH_LONG).show();

        // Clear OTP inputs
        clearOtpInputs();
    }

    private void registerUserInFirebase() {
        Log.d(TAG, "========== STARTING USER REGISTRATION ==========");
        Log.d(TAG, "Email: " + email);
        Log.d(TAG, "FirstName: " + firstName);
        Log.d(TAG, "LastName: " + lastName);
        Log.d(TAG, "Birth: " + birth);

        btnVerify.setText("Creating Account...");

        // Call backend register endpoint
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("email", email);
        registerRequest.put("password", password);
        registerRequest.put("firstName", firstName);
        registerRequest.put("lastName", lastName);
        registerRequest.put("birth", birth);

        Log.d(TAG, "Sending register request to backend: " + registerRequest);

        apiService.registerUser(registerRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Register response code: " + response.code());

                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ User registered successfully in backend!");

                    try {
                        String responseBody = response.body().string();
                        Log.d(TAG, "Response body: " + responseBody);

                        Toast.makeText(OtpVerificationActivity.this,
                                "✅ Account created successfully!",
                                Toast.LENGTH_SHORT).show();

                        // Navigate to Home
                        startActivity(new Intent(OtpVerificationActivity.this, HomeActivity.class));
                        finish();

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                        Toast.makeText(OtpVerificationActivity.this,
                                "✅ Account created!",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(OtpVerificationActivity.this, HomeActivity.class));
                        finish();
                    }

                } else {
                    Log.e(TAG, "❌ Registration failed on backend. Code: " + response.code());
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "Error body: " + errorBody);
                        Toast.makeText(OtpVerificationActivity.this,
                                "Registration failed: " + errorBody,
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(OtpVerificationActivity.this,
                                "Registration failed",
                                Toast.LENGTH_SHORT).show();
                    }

                    btnVerify.setEnabled(true);
                    btnVerify.setText("Verify");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "❌ Network error during registration: " + t.getMessage());
                Toast.makeText(OtpVerificationActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();

                btnVerify.setEnabled(true);
                btnVerify.setText("Verify");
            }
        });
    }

    private void resendOTP() {
        tvResend.setEnabled(false);
        tvResend.setAlpha(0.5f);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Map<String, String> resendRequest = new HashMap<>();
        resendRequest.put("email", email);
        resendRequest.put("firstName", firstName);

        apiService.resendOtp(resendRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(OtpVerificationActivity.this,
                            "OTP resent successfully",
                            Toast.LENGTH_SHORT).show();
                    startTimer();
                    clearOtpInputs();
                } else {
                    Toast.makeText(OtpVerificationActivity.this,
                            "Failed to resend OTP",
                            Toast.LENGTH_SHORT).show();
                    tvResend.setEnabled(true);
                    tvResend.setAlpha(1f);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(OtpVerificationActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                tvResend.setEnabled(true);
                tvResend.setAlpha(1f);
            }
        });
    }

    private void clearOtpInputs() {
        etOtp1.setText("");
        etOtp2.setText("");
        etOtp3.setText("");
        etOtp4.setText("");
        etOtp5.setText("");
        etOtp6.setText("");
        etOtp1.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}