package com.hash.bookmyseat.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hash.bookmyseat.R;
import com.hash.bookmyseat.activity.HomeActivity;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "bookmyseat_channel";
    private static final String CHANNEL_NAME = "BookMySeat Notifications";
    private static final String CHANNEL_DESCRIPTION = "Booking confirmations and updates";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification Title: " + title);
            Log.d(TAG, "Notification Body: " + body);

            sendNotification(title, body);
        }

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "New FCM Token: " + token);


        saveTokenToFirestore(token);
    }

    private void saveTokenToFirestore(String token) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("fcmToken", token);
            tokenData.put("updatedAt", System.currentTimeMillis());

            db.collection("users").document(userId)
                    .update(tokenData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ FCM token saved to Firestore for user: " + userId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to save token: " + e.getMessage());
                        // Try to create the field if it doesn't exist
                        Map<String, Object> newData = new HashMap<>();
                        newData.put("fcmToken", token);
                        newData.put("updatedAt", System.currentTimeMillis());
                        db.collection("users").document(userId)
                                .set(newData, com.google.firebase.firestore.SetOptions.merge())
                                .addOnSuccessListener(aVoid2 -> {
                                    Log.d(TAG, "✅ FCM token created in Firestore");
                                });
                    });
        } else {
            Log.d(TAG, "No user logged in, token will be saved after login");
            // Store token temporarily
            getSharedPreferences("fcm_prefs", MODE_PRIVATE)
                    .edit()
                    .putString("fcm_token", token)
                    .apply();
        }
    }


    public static void saveTokenAfterLogin(Context context) {
        String savedToken = context.getSharedPreferences("fcm_prefs", MODE_PRIVATE)
                .getString("fcm_token", null);

        if (savedToken != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("fcmToken", savedToken);
            tokenData.put("updatedAt", System.currentTimeMillis());

            FirebaseFirestore.getInstance()
                    .collection("users").document(userId)
                    .update(tokenData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FCMService", "✅ Token saved after login");
                    });
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        String type = data.get("type");
        String bookingId = data.get("bookingId");

        if ("booking_confirmation".equals(type)) {
            sendNotification("🎬 Booking Confirmed!", "Your booking " + bookingId + " is confirmed!");
        } else if ("ticket_issued".equals(type)) {
            sendNotification("🎫 Ticket Issued!", "Your ticket has been issued!");
        }
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = CHANNEL_ID;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}