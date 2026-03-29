package com.hash.bookmyseat.activity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hash.bookmyseat.R;
import com.hash.bookmyseat.adapter.EventAdapter;
import com.hash.bookmyseat.model.Event;
import com.hash.bookmyseat.service.MyFirebaseMessagingService;
import com.hash.bookmyseat.service.ShakeDetector;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView txtUserName;
    private RecyclerView rvUpcomingEvents;
    private RecyclerView rvComingSoonEvents;
    private EventAdapter upcomingAdapter;
    private EventAdapter comingSoonAdapter;
    private List<Event> upcomingList = new ArrayList<>();
    private List<Event> comingSoonList = new ArrayList<>();
    private List<Event> filteredUpcomingList = new ArrayList<>();
    private List<Event> filteredComingSoonList = new ArrayList<>();
    private EditText searchView;
    private ShakeDetector shakeDetector;
    private static final String TAG = "HomeActivity";
    private int shakeCount = 0;
    private long lastShakeToastTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check accelerometer
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer != null) {
            // Only one toast at startup
            Toast.makeText(this, "✅ Shake to refresh", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "✅ Accelerometer found");
        } else {
            Toast.makeText(this, "❌ Accelerometer not available", Toast.LENGTH_SHORT).show();
        }

        initViews();
        loadUserData();
        loadEvents();
        setupBottomNavigation();
        setupSearch();
        setupShakeToRefresh();

        getFCMToken();
        MyFirebaseMessagingService.saveTokenAfterLogin(this);
    }


    private void setupShakeToRefresh() {
        shakeDetector = new ShakeDetector(this, () -> {
            runOnUiThread(() -> {
                shakeCount++;
                Log.d(TAG, "🎯 SHAKE DETECTED! Count: " + shakeCount);


                Snackbar.make(findViewById(android.R.id.content),
                        "🔄 Refreshing events...",
                        Snackbar.LENGTH_SHORT).show();

                // Refresh events
                refreshEvents();
            });
        });
        shakeDetector.start();
        Log.d(TAG, "Shake detector initialized");
    }

    private void refreshEvents() {
        Log.d(TAG, "Refreshing events...");
        loadEvents();
    }


    private void setupSearch() {
        searchView = findViewById(R.id.searchView);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterEvents(String query) {
        filteredUpcomingList.clear();
        filteredComingSoonList.clear();

        if (query.isEmpty()) {
            filteredUpcomingList.addAll(upcomingList);
            filteredComingSoonList.addAll(comingSoonList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Event event : upcomingList) {
                if (event.getTitle().toLowerCase().contains(lowerQuery) ||
                        event.getMovieTitle().toLowerCase().contains(lowerQuery) ||
                        event.getVenue().toLowerCase().contains(lowerQuery)) {
                    filteredUpcomingList.add(event);
                }
            }
            for (Event event : comingSoonList) {
                if (event.getTitle().toLowerCase().contains(lowerQuery) ||
                        event.getMovieTitle().toLowerCase().contains(lowerQuery) ||
                        event.getVenue().toLowerCase().contains(lowerQuery)) {
                    filteredComingSoonList.add(event);
                }
            }
        }

        upcomingAdapter.updateList(filteredUpcomingList);
        comingSoonAdapter.updateList(filteredComingSoonList);
    }

    // ==================== FCM TOKEN ====================
    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Log.d(TAG, "FCM Token: " + token);
                        saveTokenToFirestore(token);
                    }
                });
    }

    private void saveTokenToFirestore(String token) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            java.util.HashMap<String, Object> tokenData = new java.util.HashMap<>();
            tokenData.put("fcmToken", token);
            tokenData.put("updatedAt", System.currentTimeMillis());

            db.collection("users").document(userId)
                    .update(tokenData)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ FCM token saved"))
                    .addOnFailureListener(e -> {
                        db.collection("users").document(userId)
                                .set(tokenData, com.google.firebase.firestore.SetOptions.merge())
                                .addOnSuccessListener(aVoid2 -> Log.d(TAG, "✅ FCM token created"));
                    });
        }
    }

    // ==================== UI INITIALIZATION ====================
    private void initViews() {
        txtUserName = findViewById(R.id.txtUserName);
        ImageView btnLogout = findViewById(R.id.btnLogout);
        rvUpcomingEvents = findViewById(R.id.rvUpcomingEvents);
        rvComingSoonEvents = findViewById(R.id.rvComingSoonEvents);

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(HomeActivity.this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        rvUpcomingEvents.setLayoutManager(new LinearLayoutManager(this));
        upcomingAdapter = new EventAdapter(filteredUpcomingList, this::navigateToMovieDetails);
        rvUpcomingEvents.setAdapter(upcomingAdapter);

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvComingSoonEvents.setLayoutManager(horizontalLayoutManager);
        rvComingSoonEvents.setHasFixedSize(true);
        rvComingSoonEvents.setNestedScrollingEnabled(false);
        comingSoonAdapter = new EventAdapter(filteredComingSoonList, this::navigateToMovieDetails);
        rvComingSoonEvents.setAdapter(comingSoonAdapter);
    }

    private void navigateToMovieDetails(Event event) {
        Intent intent = new Intent(HomeActivity.this, MovieDetailsActivity.class);
        intent.putExtra("event_id", event.getEventId());
        intent.putExtra("movie_title", event.getMovieTitle());
        intent.putExtra("event_title", event.getTitle());
        intent.putExtra("description", event.getDescription());
        intent.putExtra("date", event.getDate());
        intent.putExtra("time", event.getTime());
        intent.putExtra("venue", event.getVenue());
        intent.putExtra("location", event.getLocation());
        intent.putExtra("contact_number", event.getContactNumber());
        intent.putExtra("price_per_seat", event.getPricePerSeat());
        intent.putExtra("poster_base64", event.getPosterBase64());
        startActivity(intent);
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId != null) {
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String name = document.getString("displayName");
                                if (name == null) name = document.getString("firstName");
                                if (name != null) {
                                    txtUserName.setText("Hi, " + name + " 👋");
                                }
                            }
                        }
                    });
        }
    }

    private void loadEvents() {
        Log.d(TAG, "========== LOADING EVENTS ==========");

        // Load Upcoming Events
        db.collection("events")
                .whereEqualTo("status", "upcoming")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        upcomingList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Event event = doc.toObject(Event.class);
                            upcomingList.add(event);
                        }
                        filteredUpcomingList.clear();
                        filteredUpcomingList.addAll(upcomingList);
                        upcomingAdapter.notifyDataSetChanged();
                        Log.d(TAG, "✅ Loaded " + upcomingList.size() + " upcoming events");
                    }
                });


        db.collection("events")
                .whereEqualTo("status", "coming_soon")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        comingSoonList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Event event = doc.toObject(Event.class);
                            comingSoonList.add(event);
                        }
                        filteredComingSoonList.clear();
                        filteredComingSoonList.addAll(comingSoonList);
                        comingSoonAdapter.notifyDataSetChanged();
                        Log.d(TAG, "✅ Loaded " + comingSoonList.size() + " coming soon events");
                    }
                });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_tickets) {
                startActivity(new Intent(HomeActivity.this, MyBookingsActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shakeDetector != null) {
            shakeDetector.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (shakeDetector != null) {
            shakeDetector.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (shakeDetector != null) {
            shakeDetector.stop();
        }
    }
}