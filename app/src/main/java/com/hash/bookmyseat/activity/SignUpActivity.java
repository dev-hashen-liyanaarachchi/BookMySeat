package com.hash.bookmyseat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.hash.bookmyseat.R;
import com.hash.bookmyseat.model.ApiService;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout layoutFirstName, layoutLastName, layoutBirth, layoutEmail, layoutPassword, layoutConfirmPassword;
    private MaterialButton btnSignUp;
    private static final String BASE_URL = "http://192.168.8.190:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_sign_up);

        initViews();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSignUp.setOnClickListener(v -> validateAndSendOTP());
    }

    private void initViews() {
        layoutFirstName = findViewById(R.id.layoutFirstName);
        layoutLastName = findViewById(R.id.layoutLastName);
        layoutBirth = findViewById(R.id.layoutBirth);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPass);
        layoutConfirmPassword = findViewById(R.id.layoutConfirmPass);
        btnSignUp = findViewById(R.id.btnDoSignUp);
    }

    private void validateAndSendOTP() {
        String firstName = layoutFirstName.getEditText().getText().toString().trim();
        String lastName = layoutLastName.getEditText().getText().toString().trim();
        String birth = layoutBirth.getEditText().getText().toString().trim();
        String email = layoutEmail.getEditText().getText().toString().trim();
        String password = layoutPassword.getEditText().getText().toString().trim();
        String confirmPassword = layoutConfirmPassword.getEditText().getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(firstName)) {
            layoutFirstName.setError("First name is required");
            return;
        }
        if (TextUtils.isEmpty(lastName)) {
            layoutLastName.setError("Last name is required");
            return;
        }
        if (TextUtils.isEmpty(birth)) {
            layoutBirth.setError("Birthday is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            layoutEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            layoutPassword.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            layoutPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            layoutConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Disable button and show loading
        btnSignUp.setEnabled(false);
        btnSignUp.setText("Sending OTP...");

        // Send OTP to backend
        sendOtpToBackend(firstName, lastName, birth, email, password);
    }

    private void sendOtpToBackend(String firstName, String lastName, String birth,
                                  String email, String password) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Map<String, Object> otpRequest = new HashMap<>();
        otpRequest.put("firstName", firstName);
        otpRequest.put("lastName", lastName);
        otpRequest.put("birth", birth);
        otpRequest.put("email", email);
        otpRequest.put("password", password);

        apiService.sendOtp(otpRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnSignUp.setEnabled(true);
                btnSignUp.setText("Sign Up");

                if (response.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this,
                            "OTP sent to " + email,
                            Toast.LENGTH_LONG).show();

                    // Go to OTP Verification Activity
                    Intent intent = new Intent(SignUpActivity.this, OtpVerificationActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("firstName", firstName);
                    intent.putExtra("lastName", lastName);
                    intent.putExtra("birth", birth);
                    intent.putExtra("password", password);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(SignUpActivity.this,
                            "Failed to send OTP. Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSignUp.setEnabled(true);
                btnSignUp.setText("Sign Up");

                Toast.makeText(SignUpActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}