package com.hash.bookmyseat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hash.bookmyseat.R;
import com.hash.bookmyseat.model.Seat;
import com.hash.bookmyseat.model.SeatAdapter;

import java.util.ArrayList;
import java.util.List;

// PayHere Libraries - v3.0.18
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;

public class SeatSelectionActivity extends AppCompatActivity {

    private List<Seat> seatList = new ArrayList<>();
    private int selectedCount = 0;
    private int pricePerSeat = 1200;
    private TextView txtTotalPrice;
    private String movieTitle;
    private String eventId;
    private final static int PAYHERE_REQUEST = 11001;
    private static final String TAG = "SeatSelectionActivity";
    private static final String MERCHANT_ID = "1224292";
    private String currentOrderId;
    private FirebaseFirestore db;
    private List<String> bookedSeats = new ArrayList<>();
    private boolean isLoading = true;
    private SeatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_seat_selection);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("event_id");
        movieTitle = getIntent().getStringExtra("movie_title");

        if (getIntent().hasExtra("price_per_seat")) {
            pricePerSeat = (int) getIntent().getDoubleExtra("price_per_seat", 1200);
        }

        Log.d(TAG, "Event ID: " + eventId);
        Log.d(TAG, "Movie Title: " + movieTitle);
        Log.d(TAG, "Price per seat: " + pricePerSeat);

        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        RecyclerView rvSeats = findViewById(R.id.rvSeats);
        ImageView btnBack = findViewById(R.id.btnBackSeat);
        MaterialButton btnConfirm = findViewById(R.id.btnConfirmBooking);

        btnBack.setOnClickListener(v -> finish());

        adapter = new SeatAdapter(seatList, seat -> {
            if (seat.getStatus() == 2) {
                Toast.makeText(this, "Seat " + seat.getSeatNumber() + " is already booked!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (seat.getStatus() == 0) {
                seat.setStatus(1);
                selectedCount++;
                Log.d(TAG, "Selected seat: " + seat.getSeatNumber());
            } else if (seat.getStatus() == 1) {
                seat.setStatus(0);
                selectedCount--;
                Log.d(TAG, "Deselected seat: " + seat.getSeatNumber());
            }
            updateTotal();
            adapter.notifyDataSetChanged();
        });

        rvSeats.setLayoutManager(new GridLayoutManager(this, 8));
        rvSeats.setAdapter(adapter);

        Toast.makeText(this, "Loading seat availability...", Toast.LENGTH_SHORT).show();

        loadBookedSeats(() -> {
            generateSeats();
            adapter.notifyDataSetChanged();
            isLoading = false;
            Toast.makeText(this, "Seats loaded! Booked seats are highlighted.", Toast.LENGTH_SHORT).show();
        });

        btnConfirm.setOnClickListener(v -> {
            if (isLoading) {
                Toast.makeText(this, "Please wait, loading seats...", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedCount > 0) {
                double totalAmount = (double) selectedCount * pricePerSeat;
                initiatePayment(totalAmount);
            } else {
                Toast.makeText(this, "Please select at least one seat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBookedSeats(Runnable onComplete) {
        Log.d(TAG, "Loading booked seats for event: " + eventId);

        db.collection("bookings")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "PAID")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookedSeats.clear();
                        int count = 0;
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            // Get seats from selectedSeats field (ARRAY)
                            List<String> seatsList = (List<String>) doc.get("selectedSeats");

                            if (seatsList != null && !seatsList.isEmpty()) {
                                for (String seat : seatsList) {
                                    String seatTrimmed = seat.trim();
                                    if (!bookedSeats.contains(seatTrimmed)) {
                                        bookedSeats.add(seatTrimmed);
                                        count++;
                                        Log.d(TAG, "Booked seat found: " + seatTrimmed);
                                    }
                                }
                            } else {
                                // Fallback: try seats field (string)
                                String seats = doc.getString("seats");
                                if (seats != null && !seats.isEmpty()) {
                                    String[] seatArray = seats.split(",\\s*");
                                    for (String seat : seatArray) {
                                        String seatTrimmed = seat.trim();
                                        if (!bookedSeats.contains(seatTrimmed)) {
                                            bookedSeats.add(seatTrimmed);
                                            count++;
                                            Log.d(TAG, "Booked seat found (fallback): " + seatTrimmed);
                                        }
                                    }
                                }
                            }
                        }
                        Log.d(TAG, "Loaded " + count + " already booked seats");
                        Log.d(TAG, "Booked seats list: " + bookedSeats.toString());
                    } else {
                        Log.e(TAG, "Failed to load booked seats", task.getException());
                    }
                    onComplete.run();
                });
    }

    private void generateSeats() {
        seatList.clear();
        Log.d(TAG, "Generating seats, booked seats count: " + bookedSeats.size());

        for (int i = 1; i <= 40; i++) {
            String seatNumber = "S" + i;
            int status;

            if (bookedSeats.contains(seatNumber)) {
                status = 2; // Already booked - RED
                Log.d(TAG, "Seat " + seatNumber + " is BOOKED");
            } else {
                status = 0; // Available - YELLOW
            }

            seatList.add(new Seat(seatNumber, status));
        }

        Log.d(TAG, "Total seats generated: " + seatList.size());
    }

    private void initiatePayment(double amount) {
        try {
            InitRequest req = new InitRequest();

            req.setMerchantId(MERCHANT_ID);
            req.setCurrency("LKR");
            req.setAmount(amount);

            currentOrderId = "MS-" + System.currentTimeMillis();
            req.setOrderId(currentOrderId);

            String description = movieTitle != null ? movieTitle + " Tickets" : "Movie Tickets";
            req.setItemsDescription(description);

            Item item = new Item();
            item.setName(movieTitle != null ? movieTitle : "Movie Ticket");
            item.setQuantity(selectedCount);
            item.setAmount(amount);
            req.getItems().add(item);

            String userEmail = "customer@email.com";
            String userPhone = "0771234567";
            String userName = "Guest User";

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                if (FirebaseAuth.getInstance().getCurrentUser().getEmail() != null) {
                    userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                }
                if (FirebaseAuth.getInstance().getCurrentUser().getDisplayName() != null) {
                    userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                }
            }

            String firstName = "Guest";
            String lastName = "User";

            if (!userName.equals("Guest User")) {
                String[] nameParts = userName.split(" ");
                if (nameParts.length > 0) {
                    firstName = nameParts[0];
                    lastName = nameParts.length > 1 ? nameParts[1] : "User";
                }
            }

            req.getCustomer().setFirstName(firstName);
            req.getCustomer().setLastName(lastName);
            req.getCustomer().setEmail(userEmail);
            req.getCustomer().setPhone(userPhone);
            req.getCustomer().getAddress().setAddress("No 1, Main Street");
            req.getCustomer().getAddress().setCity("Colombo");
            req.getCustomer().getAddress().setCountry("Sri Lanka");

            PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);

            Intent intent = new Intent(this, PHMainActivity.class);
            intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
            startActivityForResult(intent, PAYHERE_REQUEST);

            Log.d(TAG, "Payment initiated for amount: " + amount + ", Order ID: " + currentOrderId);

        } catch (Exception e) {
            Log.e(TAG, "Error initiating payment: " + e.getMessage());
            Toast.makeText(this, "Error initiating payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYHERE_REQUEST) {
            if (data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                PHResponse<?> response = (PHResponse<?>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

                if (response != null) {
                    int status = response.getStatus();
                    Log.d(TAG, "PayHere Response - Status: " + status);

                    if (status == 1) {
                        Log.d(TAG, "✅ Payment Successful!");

                        StringBuilder selectedSeatsString = new StringBuilder();
                        for (Seat s : seatList) {
                            if (s.getStatus() == 1) {
                                if (selectedSeatsString.length() > 0) selectedSeatsString.append(", ");
                                selectedSeatsString.append(s.getSeatNumber());
                            }
                        }

                        double totalAmount = selectedCount * pricePerSeat;
                        String orderId = currentOrderId != null ? currentOrderId : "MS-" + System.currentTimeMillis();
                        String paymentId = "PAYHERE_" + System.currentTimeMillis();

                        Intent intent = new Intent(SeatSelectionActivity.this, SuccessActivity.class);
                        intent.putExtra("movie_title", movieTitle);
                        intent.putExtra("selected_seats", selectedSeatsString.toString());
                        intent.putExtra("total_price", totalAmount);
                        intent.putExtra("order_id", orderId);
                        intent.putExtra("payment_id", paymentId);
                        intent.putExtra("event_id", eventId);

                        startActivity(intent);
                        finish();

                    } else if (status == -1) {
                        Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
                    } else if (status == 2) {
                        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Payment Status: " + status, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response is null");
                    Toast.makeText(this, "Payment failed: No response from PayHere", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "No data in intent");
                Toast.makeText(this, "Payment failed: No data received", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateTotal() {
        int total = selectedCount * pricePerSeat;
        txtTotalPrice.setText("Rs. " + total + ".00");
    }
}