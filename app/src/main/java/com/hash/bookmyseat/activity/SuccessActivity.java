package com.hash.bookmyseat.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.hash.bookmyseat.R;
import com.hash.bookmyseat.model.ApiService;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SuccessActivity extends AppCompatActivity {

    private String movieTitle, selectedSeats, userId, userEmail, eventId;
    private double totalAmount;
    private static final String TAG = "SuccessActivity";
    private static final String BASE_URL = "http://192.168.8.190:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);


        eventId = getIntent().getStringExtra("event_id");
        Log.d(TAG, "Event ID received: " + eventId);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if (userEmail == null || userEmail.isEmpty()) {
                userEmail = "user@example.com";
            }
        } else {
            userId = "GUEST_" + System.currentTimeMillis();
            userEmail = "guest@example.com";
        }

        ImageView imgQRCode = findViewById(R.id.imgQRCode);
        TextView txtMovie = findViewById(R.id.txtSuccessMovie);
        TextView txtDetails = findViewById(R.id.txtSuccessDetails);
        MaterialButton btnHome = findViewById(R.id.btnBackToHome);

        movieTitle = getIntent().getStringExtra("movie_title");
        selectedSeats = getIntent().getStringExtra("selected_seats");
        totalAmount = getIntent().getDoubleExtra("total_price", 0.0);

        if (movieTitle == null) movieTitle = "Movie";
        if (selectedSeats == null) selectedSeats = "N/A";

        txtMovie.setText(movieTitle);
        txtDetails.setText("Seats: " + selectedSeats + "\nTotal: LKR " + totalAmount);

        String bookingId = "BMS-" + System.currentTimeMillis();
        generateQRCode(bookingId, imgQRCode);


        sendBookingToBackend(bookingId, movieTitle, userEmail, selectedSeats, totalAmount, eventId);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(SuccessActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void generateQRCode(String data, ImageView imageView) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400);
            imageView.setImageBitmap(bitmap);
            Log.d(TAG, "QR Code generated successfully");
        } catch (Exception e) {
            Log.e(TAG, "QR Error: " + e.getMessage());
        }
    }

    private void sendBookingToBackend(String bookingId, String title, String email,
                                      String seats, double amount, String eventId) {
        Log.d(TAG, "Sending booking to: " + BASE_URL);
        Log.d(TAG, "EventId: " + eventId);

        try {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);

            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("bookingId", bookingId);
            bookingData.put("userId", userId);
            bookingData.put("movieTitle", title);
            bookingData.put("email", email);
            bookingData.put("seats", seats);
            bookingData.put("totalAmount", amount);
            bookingData.put("status", "PAID");
            bookingData.put("eventId", eventId);  // ← IMPORTANT: Save eventId

            Log.d(TAG, "Booking data: " + bookingData);

            Call<ResponseBody> call = apiService.confirmBooking(bookingData);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Backend success");
                        Toast.makeText(SuccessActivity.this,
                                "✅ Booking confirmed!",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Log.e(TAG, "Backend error: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "Connection failed: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SuccessActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}