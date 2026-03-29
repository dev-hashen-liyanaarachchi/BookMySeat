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

    private static final String BASE_URL = "http://192.168.8.190:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
            return;
        }

        initViews();
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
        tvSignUp.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        btnGoogle.setOnClickListener(v -> Toast.makeText(this, "Google Sign In coming soon!", Toast.LENGTH_SHORT).show());
    }

    private void loginUser() {
        String email = layoutEmail.getEditText().getText().toString().trim();
        String password = layoutPassword.getEditText().getText().toString().trim();

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

        btnLogin.setEnabled(false);
        btnLogin.setText("Signing In...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Sign In");

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Welcome " + user.getEmail() + "!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                        Toast.makeText(LoginActivity.this, "Login Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showForgotPasswordDialog() {
        String email = layoutEmail.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            layoutEmail.setError("Enter your email to reset password");
            layoutEmail.requestFocus();
            return;
        }

        // Send forgot password request to backend
        btnLogin.setEnabled(false);
        btnLogin.setText("Sending...");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        apiService.forgotPassword(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Sign In");

                if (response.isSuccessful()) {
                    Toast.makeText(LoginActivity.this,
                            "Temporary password sent to " + email + ". Please check your email.",
                            Toast.LENGTH_LONG).show();

                    // Show dialog to enter temporary password and new password
                    showResetPasswordDialog(email);
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Email not found. Please register first.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Sign In");
                Toast.makeText(LoginActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResetPasswordDialog(String email) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        View view = getLayoutInflater().inflate(R.layout.dialog_reset_password, null);
        com.google.android.material.textfield.TextInputLayout layoutTempPassword = view.findViewById(R.id.layoutTempPassword);
        com.google.android.material.textfield.TextInputLayout layoutNewPassword = view.findViewById(R.id.layoutNewPassword);
        com.google.android.material.textfield.TextInputLayout layoutConfirmPassword = view.findViewById(R.id.layoutConfirmPassword);

        builder.setView(view);
        builder.setPositiveButton("Reset Password", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        android.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            android.widget.Button button = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String tempPassword = layoutTempPassword.getEditText().getText().toString().trim();
                String newPassword = layoutNewPassword.getEditText().getText().toString().trim();
                String confirmPassword = layoutConfirmPassword.getEditText().getText().toString().trim();

                if (TextUtils.isEmpty(tempPassword)) {
                    layoutTempPassword.setError("Temporary password required");
                    return;
                }
                if (TextUtils.isEmpty(newPassword)) {
                    layoutNewPassword.setError("New password required");
                    return;
                }
                if (newPassword.length() < 6) {
                    layoutNewPassword.setError("Password must be at least 6 characters");
                    return;
                }
                if (!newPassword.equals(confirmPassword)) {
                    layoutConfirmPassword.setError("Passwords do not match");
                    return;
                }

                // Verify temp password and set new password
                verifyTempPasswordAndReset(email, tempPassword, newPassword, dialog);
            });
        });

        dialog.show();
    }

    private void verifyTempPasswordAndReset(String email, String tempPassword, String newPassword, android.app.AlertDialog dialog) {
        // First verify with temporary password
        mAuth.signInWithEmailAndPassword(email, tempPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update password
                        FirebaseUser user = mAuth.getCurrentUser();
                        user.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this,
                                                "Password reset successfully! Please login with new password.",
                                                Toast.LENGTH_LONG).show();
                                        mAuth.signOut();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(LoginActivity.this,
                                                "Failed to reset password: " + updateTask.getException().getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Invalid temporary password. Please check your email.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}