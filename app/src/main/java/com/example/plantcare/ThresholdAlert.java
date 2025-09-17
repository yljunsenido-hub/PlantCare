package com.example.plantcare;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ThresholdAlert extends AppCompatActivity {

    private static final String CHANNEL_ID = "PlantAlerts";
    private EditText inputMoisture, inputTemp, inputHumid;
    private Button btnApply;
    private DatabaseReference alertsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_threshold_alert);

        inputMoisture = findViewById(R.id.inputMoisture);
        inputTemp = findViewById(R.id.inputTemp);
        inputHumid = findViewById(R.id.inputHumid);
        btnApply = findViewById(R.id.btnApply);

        createNotificationChannel();
        requestNotifPermission();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        alertsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(uid)
                .child("alerts");

        // 🔹 Listen for soil + temp changes
        alertsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("soil")) {
                        String soilMsg = snapshot.child("soil").getValue(String.class);
                        showNotification("Soil Alert", soilMsg);
                    }
                    if (snapshot.hasChild("temp")) {
                        String tempMsg = snapshot.child("temp").getValue(String.class);
                        showNotification("Temperature Alert", tempMsg);
                    }
                    if (snapshot.hasChild("humid")) {
                        String humidMsg = snapshot.child("humid").getValue(String.class);
                        showNotification("Humidity Alert", humidMsg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // 🔹 Send thresholds to ESP8266
        btnApply.setOnClickListener(v -> {
            String mStr = inputMoisture.getText().toString().trim();
            String tStr = inputTemp.getText().toString().trim();
            String hStr = inputHumid.getText().toString().trim();

            if (mStr.isEmpty() || tStr.isEmpty() || hStr.isEmpty()) {
                Toast.makeText(this, "Enter thresholds", Toast.LENGTH_SHORT).show();
                return;
            }

            int moisture = Integer.parseInt(mStr);
            int temp = Integer.parseInt(tStr);
            int humid = Integer.parseInt(hStr);

            new Thread(() -> {
                try {
                    String url = "http://esp8266.local/setThreshold?moisture=" + moisture + "&temp=" + temp + "&humid=" + humid;
                    java.net.URL u = new java.net.URL(url);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                    conn.setRequestMethod("GET");
                    conn.getInputStream();
                    runOnUiThread(() -> Toast.makeText(this, "Thresholds sent to ESP", Toast.LENGTH_SHORT).show());
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();

        });
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Plant Alerts";
            String description = "Threshold notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private void requestNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void showNotification(String title, String message) {
        if (message == null) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.plantcarelogo) // your app logo from res/drawable
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify((int) System.currentTimeMillis(), builder.build());
    }
}
