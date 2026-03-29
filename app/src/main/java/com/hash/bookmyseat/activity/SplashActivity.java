package com.hash.bookmyseat.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.hash.bookmyseat.R;

public class SplashActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST = 101;
    private FirebaseAuth mAuth;
    private static final int SPLASH_DURATION = 2500;
    private ViewFlipper viewFlipper;
    private int[] images = {R.drawable.ic_splash_logo, R.drawable.ic_splash_logo, R.drawable.ic_splash_logo};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();


        ImageView ivLogo = findViewById(R.id.ivSplashLogo);
        TextView txtLogo = findViewById(R.id.txtLogo);
        TextView txtTitle = findViewById(R.id.txtTitle);
        TextView txtSubTitle = findViewById(R.id.txtSubTitle);
        MaterialButton btnSignIn = findViewById(R.id.btnSignIn);
        MaterialButton btnSignUp = findViewById(R.id.btnSignUp);
        View layoutDots = findViewById(R.id.layoutDots);


        btnSignIn.setVisibility(View.GONE);
        btnSignUp.setVisibility(View.GONE);
        layoutDots.setVisibility(View.GONE);


        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale);


        ivLogo.startAnimation(scaleAnimation);
        txtLogo.startAnimation(fadeIn);
        txtTitle.startAnimation(fadeIn);
        txtSubTitle.startAnimation(fadeIn);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST);
            }
        }


        new Handler().postDelayed(() -> {
            if (mAuth.getCurrentUser() != null) {
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                finish();
            } else {

                btnSignIn.setVisibility(View.VISIBLE);
                btnSignUp.setVisibility(View.VISIBLE);
                layoutDots.setVisibility(View.VISIBLE);

                btnSignIn.startAnimation(slideUp);
                btnSignUp.startAnimation(slideUp);
                layoutDots.startAnimation(fadeIn);
            }
        }, SPLASH_DURATION);

        btnSignIn.setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        });

        btnSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, SignUpActivity.class));
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }
}