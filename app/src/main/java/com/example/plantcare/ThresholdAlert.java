package com.example.plantcare;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.concurrent.atomic.AtomicInteger;


public class ThresholdAlert extends AppCompatActivity {

    private static final String CHANNEL_ID = "PlantAlerts";
    private static final AtomicInteger NOTIF_ID = new AtomicInteger();
    private EditText inputMoisture, inputTemp;
    private Button btnApply;
    private DatabaseReference alertRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_threshold_alert);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inputMoisture = findViewById(R.id.inputMoisture);
        inputTemp = findViewById(R.id.inputTemp);
        btnApply = findViewById(R.id.btnApply);

        createNotificationChannel();

        // 🔹 Listen for alerts in Firebase
        alertRef = FirebaseDatabase.getInstance().getReference("alerts");
        alertRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot alertSnap : snapshot.getChildren()) {
                    String msg = alertSnap.getValue(String.class);
                    if (msg != null) {
                        showNotification("Threshold Alert", msg);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        // 🔹 Apply thresholds (send to ESP)
        btnApply.setOnClickListener(v -> {
            String mStr = inputMoisture.getText().toString().trim();
            String tStr = inputTemp.getText().toString().trim();
            if (mStr.isEmpty() || tStr.isEmpty()) {
                Toast.makeText(this, "Enter thresholds", Toast.LENGTH_SHORT).show();
                return;
            }

            int moisture = Integer.parseInt(mStr);
            int temp = Integer.parseInt(tStr);

            new Thread(() -> {
                try {
                    String url = "http://esp8266.local/setThreshold?moisture=" + moisture + "&temp=" + temp;
                    java.net.URL u = new java.net.URL(url);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                    conn.setRequestMethod("GET");
                    conn.getInputStream(); // trigger call
                    runOnUiThread(() -> Toast.makeText(this, "Thresholds sent", Toast.LENGTH_SHORT).show());
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Plant Alerts";
            String description = "Notifications for threshold alerts";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String message) {
        int id = NOTIF_ID.incrementAndGet();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert) // test icon
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(id, builder.build());
        }
    }
}
