package com.example.plantcare;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ThresholdAlert extends AppCompatActivity {
    private EditText inputMoisture, inputTemp, inputHumid;
    private Button btnApply;
    private DatabaseReference thresholdsRef, alertsRef;
    private TextView moistureText, tempText, humidText;

    private String uid;
    private ImageButton btnBack;
    private String espUrl = "http://esp8266.local"; // ESP endpoint
    private static final String CHANNEL_ID = "threshold_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_threshold_alert);

        inputMoisture = findViewById(R.id.inputMoisture);
        inputTemp = findViewById(R.id.inputTemp);
        inputHumid = findViewById(R.id.inputHumid);
        btnApply = findViewById(R.id.btnApply);
        btnBack = findViewById(R.id.btnBack);

        moistureText = findViewById(R.id.moisture);
        tempText = findViewById(R.id.temperature);
        humidText = findViewById(R.id.humidity);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ThresholdAlert.this, Homepage.class);
            startActivity(intent);
            finish();
        });

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        thresholdsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(uid)
                .child("thresholds");
        alertsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(uid)
                .child("alerts");

        createNotificationChannel();

        btnApply.setOnClickListener(v -> saveThresholds());

        // Load current thresholds on page open
        loadThresholds();

        // Start listening for alerts pushed by Arduino
        listenForAlerts();
    }

    private void saveThresholds() {
        String mStr = inputMoisture.getText().toString().trim();
        String tStr = inputTemp.getText().toString().trim();
        String hStr = inputHumid.getText().toString().trim();

        if (TextUtils.isEmpty(mStr) || TextUtils.isEmpty(tStr) || TextUtils.isEmpty(hStr)) {
            showNotification("Threshold Error", "Please fill all fields before applying.");
            return;
        }

        int moisture = Integer.parseInt(mStr);
        int temp = Integer.parseInt(tStr);
        int humid = Integer.parseInt(hStr);

        // 1. Save to Firebase
        thresholdsRef.child("moisture").setValue(moisture);
        thresholdsRef.child("temp").setValue(temp);
        thresholdsRef.child("humid").setValue(humid);

        // 2. Send to ESP8266
        String url = espUrl + "/setThreshold?uid=" + uid
                + "&moisture=" + mStr
                + "&temp=" + tStr
                + "&humid=" + hStr;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    // Update UI
                    moistureText.setText(mStr);
                    tempText.setText(tStr);
                    humidText.setText(hStr);

                    // Clear edit texts
                    inputMoisture.setText("");
                    inputTemp.setText("");
                    inputHumid.setText("");
                },
                error -> showNotification("ESP Error", "Failed to send to ESP: " + error.getMessage()));
        queue.add(request);
    }

    private void loadThresholds() {
        thresholdsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String moisture = String.valueOf(snapshot.child("moisture").getValue());
                    String temp = String.valueOf(snapshot.child("temp").getValue());
                    String humid = String.valueOf(snapshot.child("humid").getValue());

                    if (!moisture.equals("null")) moistureText.setText(moisture);
                    if (!temp.equals("null")) tempText.setText(temp);
                    if (!humid.equals("null")) humidText.setText(humid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showNotification("Load Error", "Failed to load thresholds");
            }
        });
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
                .setSmallIcon(R.drawable.plantcarelogo) // make sure this exists in res/drawable
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // long text support
                .setPriority(NotificationCompat.PRIORITY_HIGH);

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
}
