package com.hash.bookmyseat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputLayout layoutEmail, layoutPassword;
    private MaterialButton btnLogin, btnGoogle;
    private TextView tvForgotPassword, tvSignUp;
    private View btnBack;

    private static final String BASE_URL = "http://192.168.8.190:8080/"; // Your backend IP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
            return;
        }

        // Initialize Views
        initViews();

        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnLogin.setOnClickListener(v -> loginUser());

        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            forgotPassword();
        });

        btnGoogle.setOnClickListener(v -> {
            Toast.makeText(this, "Google Sign In coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loginUser() {
        String email = layoutEmail.getEditText().getText().toString().trim();
        String password = layoutPassword.getEditText().getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            layoutEmail.setError("Email is required");
            layoutEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            layoutPassword.setError("Password is required");
            layoutPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            layoutPassword.setError("Password must be at least 6 characters");
            layoutPassword.requestFocus();
            return;
        }

        // Disable button and show loading
        btnLogin.setEnabled(false);
        btnLogin.setText("Signing In...");

        // Firebase Sign In
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();

                        Toast.makeText(LoginActivity.this,
                                "Welcome " + user.getEmail() + "!",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();

                    } else {
                        // Sign in failed
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Sign In");

                        String errorMessage;
                        try {
                            errorMessage = task.getException().getMessage();
                        } catch (Exception e) {
                            errorMessage = "Authentication failed";
                        }

                        Toast.makeText(LoginActivity.this,
                                "Login Failed: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void forgotPassword() {
        String email = layoutEmail.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            layoutEmail.setError("Enter your email to reset password");
            layoutEmail.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                "Password reset email sent to " + email,
                                Toast.LENGTH_LONG).show();
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Failed to send reset email";
                        Toast.makeText(LoginActivity.this,
                                "Error: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}