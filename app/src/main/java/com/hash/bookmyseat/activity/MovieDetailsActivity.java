package com.hash.bookmyseat.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.hash.bookmyseat.R;

import android.Manifest;

public class MovieDetailsActivity extends AppCompatActivity {

    private String eventId, movieTitle, eventTitle, description, date, time, venue, location, contactNumber;
    private double pricePerSeat;
    private String posterBase64;
    private static final int CALL_PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_movie_details);

        // Get data from intent
        eventId = getIntent().getStringExtra("event_id");
        movieTitle = getIntent().getStringExtra("movie_title");
        eventTitle = getIntent().getStringExtra("event_title");
        description = getIntent().getStringExtra("description");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        venue = getIntent().getStringExtra("venue");
        location = getIntent().getStringExtra("location");
        contactNumber = getIntent().getStringExtra("contact_number");
        pricePerSeat = getIntent().getDoubleExtra("price_per_seat", 1200);
        posterBase64 = getIntent().getStringExtra("poster_base64");

        // UI Elements
        ImageView btnBack = findViewById(R.id.btnBackDetails);
        ImageView ivPoster = findViewById(R.id.ivPoster);
        TextView txtTitle = findViewById(R.id.txtDetailsTitle);
        TextView txtMovieTitle = findViewById(R.id.txtMovieTitle);
        TextView txtDescription = findViewById(R.id.txtDescription);
        TextView txtDate = findViewById(R.id.txtDate);
        TextView txtTime = findViewById(R.id.txtTime);
        TextView txtVenue = findViewById(R.id.txtVenue);
        TextView txtLocation = findViewById(R.id.txtLocation);
        TextView txtContact = findViewById(R.id.txtContact);
        TextView txtPrice = findViewById(R.id.txtPrice);
        MaterialButton btnBookNow = findViewById(R.id.btnBookNow);
        MaterialButton btnOpenMap = findViewById(R.id.btnOpenMap);
        MaterialButton btnCall = findViewById(R.id.btnCall);

        // Set data
        txtTitle.setText(eventTitle);
        txtMovieTitle.setText(movieTitle);
        txtDescription.setText(description);
        txtDate.setText("Date: " + date);
        txtTime.setText("Time: " + time);
        txtVenue.setText("Venue: " + venue);

        // Set location
        if (location != null && !location.isEmpty()) {
            txtLocation.setText("📍 " + location);
            txtLocation.setVisibility(android.view.View.VISIBLE);
            btnOpenMap.setVisibility(android.view.View.VISIBLE);
        } else {
            txtLocation.setVisibility(android.view.View.GONE);
            btnOpenMap.setVisibility(android.view.View.GONE);
        }

        // Set contact number
        if (contactNumber != null && !contactNumber.isEmpty()) {
            txtContact.setText("📞 " + contactNumber);
            txtContact.setVisibility(android.view.View.VISIBLE);
            btnCall.setVisibility(android.view.View.VISIBLE);
        } else {
            txtContact.setVisibility(android.view.View.GONE);
            btnCall.setVisibility(android.view.View.GONE);
        }

        txtPrice.setText("Price: LKR " + pricePerSeat + " per seat");

        // Load Base64 image
        if (posterBase64 != null && !posterBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(posterBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivPoster.setImageBitmap(bitmap);
            } catch (Exception e) {
                ivPoster.setImageResource(R.drawable.ic_movie_placeholder);
            }
        } else {
            ivPoster.setImageResource(R.drawable.ic_movie_placeholder);
        }

        btnBack.setOnClickListener(v -> finish());

        btnBookNow.setOnClickListener(v -> {
            Intent intent = new Intent(MovieDetailsActivity.this, SeatSelectionActivity.class);
            intent.putExtra("event_id", eventId);
            intent.putExtra("movie_title", movieTitle);
            intent.putExtra("price_per_seat", pricePerSeat);
            startActivity(intent);
        });

        btnOpenMap.setOnClickListener(v -> openMap());
        btnCall.setOnClickListener(v -> makeCall());
    }

    private void openMap() {
        if (location == null || location.isEmpty()) {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(location));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Uri webUri = Uri.parse("https://maps.google.com/?q=" + Uri.encode(location));
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                startActivity(webIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open maps", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeCall() {
        if (contactNumber == null || contactNumber.isEmpty()) {
            Toast.makeText(this, "Contact number not available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Format phone number
            String phoneNumber = contactNumber.trim();

            // Remove all non-digit characters except +
            phoneNumber = phoneNumber.replaceAll("[^0-9+]", "");

            // Format for Sri Lanka
            if (phoneNumber.startsWith("0")) {
                phoneNumber = "+94" + phoneNumber.substring(1);
            } else if (phoneNumber.startsWith("7") && phoneNumber.length() == 9) {
                phoneNumber = "+94" + phoneNumber;
            } else if (phoneNumber.startsWith("94") && !phoneNumber.startsWith("+")) {
                phoneNumber = "+" + phoneNumber;
            } else if (!phoneNumber.startsWith("+")) {
                phoneNumber = "+94" + phoneNumber;
            }

            android.util.Log.d("MovieDetails", "Phone number formatted: " + phoneNumber);

            // Create intent with ACTION_DIAL
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));

            // Check if there's any app that can handle this intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Fallback: Try with ACTION_CALL (requires permission)
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(callIntent);
                } else {
                    // Request permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            CALL_PERMISSION_REQUEST);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MovieDetails", "Call error: " + e.getMessage());
            Toast.makeText(this, "Unable to make call: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeCall(); // Try again after permission granted
            } else {
                Toast.makeText(this, "Call permission required to make calls", Toast.LENGTH_SHORT).show();
            }
        }
    }
}