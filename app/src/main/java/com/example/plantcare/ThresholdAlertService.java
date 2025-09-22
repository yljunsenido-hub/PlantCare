package com.example.plantcare;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ThresholdAlertService extends Service {
    private static final String CHANNEL_ID = "threshold_channel";
    private DatabaseReference alertsRef;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        alertsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("alerts");

        listenForAlerts();
    }

    private void listenForAlerts() {
        alertsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String prevChildKey) {
                String message = snapshot.getValue(String.class);
                if (message != null) {
                    showNotification("Alert", message);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String prevChildKey) {
                String message = snapshot.getValue(String.class);
                if (message != null) {
                    showNotification("Alert Update", message);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String key = snapshot.getKey();
                showNotification("Alert Cleared", key + " alert cleared");
            }

            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String prevChildKey) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.plantcarelogo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Threshold Channel";
            String description = "Channel for threshold alerts";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
