package com.hash.bookmyseat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hash.bookmyseat.R;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvUserSince;
    private MaterialButton btnLogout, btnMyBookings;
    private ImageView btnBack;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        loadUserData();
        setClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserSince = findViewById(R.id.tvUserSince);
        btnLogout = findViewById(R.id.btnLogout);
        btnMyBookings = findViewById(R.id.btnMyBookings);
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String email = user.getEmail();
            tvUserEmail.setText(email);


            String userId = user.getUid();
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String name = document.getString("displayName");
                                if (name == null) name = document.getString("firstName");
                                if (name != null) {
                                    tvUserName.setText(name);
                                } else {
                                    tvUserName.setText(email != null ? email.split("@")[0] : "User");
                                }


                                Object createdAt = document.get("createdAt");
                                if (createdAt != null) {
                                    tvUserSince.setText("Member since: " + createdAt.toString().substring(0, 10));
                                } else {
                                    tvUserSince.setText("Member since: 2024");
                                }
                            } else {
                                tvUserName.setText(email != null ? email.split("@")[0] : "User");
                                tvUserSince.setText("Member since: 2024");
                            }
                        } else {
                            tvUserName.setText(email != null ? email.split("@")[0] : "User");
                        }
                    });
        }
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnMyBookings.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MyBookingsActivity.class);
            startActivity(intent);
        });
    }
}