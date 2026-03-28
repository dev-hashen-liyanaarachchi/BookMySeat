package com.hash.bookmyseat.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hash.bookmyseat.R;
import com.hash.bookmyseat.adapter.BookingsHistoryAdapter;
import com.hash.bookmyseat.model.BookingHistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyBookingsActivity extends AppCompatActivity {

    private RecyclerView rvBookings;
    private TextView tvNoBookings;
    private ImageView btnBack;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private BookingsHistoryAdapter adapter;
    private List<BookingHistory> bookingList = new ArrayList<>();
    private static final String TAG = "MyBookings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_my_bookings);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        loadBookings();
    }

    private void initViews() {
        rvBookings = findViewById(R.id.rvBookings);
        tvNoBookings = findViewById(R.id.tvNoBookings);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingsHistoryAdapter(bookingList, booking -> {
            // Navigate to booking details if needed
            Log.d(TAG, "Booking clicked: " + booking.getBookingId());
        });
        rvBookings.setAdapter(adapter);
    }

    private void loadBookings() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        Log.d(TAG, "Loading bookings for user: " + userId);

        if (userId == null) {
            Log.d(TAG, "User not logged in");
            showNoBookings();
            return;
        }

        // Removed orderBy to avoid index requirement
        db.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookingList.clear();
                        int count = 0;

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            count++;

                            // Handle bookingDate (could be String or Timestamp)
                            String bookingDateStr;
                            Object bookingDateObj = doc.get("bookingDate");
                            if (bookingDateObj instanceof com.google.firebase.Timestamp) {
                                com.google.firebase.Timestamp ts = (com.google.firebase.Timestamp) bookingDateObj;
                                bookingDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(ts.toDate());
                            } else {
                                bookingDateStr = String.valueOf(bookingDateObj);
                            }

                            BookingHistory booking = new BookingHistory(
                                    doc.getString("bookingId"),
                                    doc.getString("movieTitle"),
                                    doc.getString("seats"),
                                    doc.getDouble("totalAmount") != null ? doc.getDouble("totalAmount") : 0.0,
                                    bookingDateStr,
                                    doc.getBoolean("ticketIssued") != null ? doc.getBoolean("ticketIssued") : false,
                                    doc.getBoolean("attended") != null ? doc.getBoolean("attended") : false
                            );
                            bookingList.add(booking);

                            Log.d(TAG, "Booking " + count + ": " + booking.getBookingId() + " - " + booking.getMovieTitle());
                        }

                        Log.d(TAG, "Total bookings found: " + count);

                        if (bookingList.isEmpty()) {
                            showNoBookings();
                        } else {
                            showBookings();
                        }
                        adapter.notifyDataSetChanged();

                    } else {
                        Log.e(TAG, "Failed to load bookings: " + task.getException());
                        Toast.makeText(this, "Failed to load bookings: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showNoBookings() {
        tvNoBookings.setVisibility(android.view.View.VISIBLE);
        rvBookings.setVisibility(android.view.View.GONE);
        Log.d(TAG, "No bookings found");
    }

    private void showBookings() {
        tvNoBookings.setVisibility(android.view.View.GONE);
        rvBookings.setVisibility(android.view.View.VISIBLE);
        Log.d(TAG, "Showing " + bookingList.size() + " bookings");
    }
}