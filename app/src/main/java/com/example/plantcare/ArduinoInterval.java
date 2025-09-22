package com.example.plantcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;


public class ArduinoInterval extends AppCompatActivity {

    private TextView secondsText;
    private LinearLayout editSection;
    private EditText inputValue;
    ImageButton btnBack;
    private Button btnAdd, btnReset, btnPlus, btnMinus, btnApply;
    private DatabaseReference intervalRef;
    private String uid;

    private static final String ESP_IP = "esp8266.local";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_arduino_interval);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        secondsText = findViewById(R.id.seconds);
        editSection = findViewById(R.id.editSection);
        inputValue = findViewById(R.id.inputValue);
        btnBack = findViewById(R.id.btnBack);

        btnAdd = findViewById(R.id.btnAdd);
        btnReset = findViewById(R.id.btnReset);
        btnPlus = findViewById(R.id.btnPlus);
        btnMinus = findViewById(R.id.btnMinus);
        btnApply = findViewById(R.id.btnApply);

        btnAdd.setOnClickListener(v -> editSection.setVisibility(View.VISIBLE));

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        intervalRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(uid)
                .child("interval");

        intervalRef.child("current").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int savedValue = snapshot.getValue(Integer.class);
                    secondsText.setText(String.valueOf(savedValue));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle error if needed
            }
        });


        btnReset.setOnClickListener(v -> {
            secondsText.setText("0");
            inputValue.setText("");
            editSection.setVisibility(View.GONE);

            // Also update Firebase
            intervalRef.child("current").setValue(0);
        });


        btnPlus.setOnClickListener(v -> {
            int current = getInputValue();
            current += 5;
            inputValue.setText(String.valueOf(current));
        });

        btnMinus.setOnClickListener(v -> {
            int current = getInputValue();
            current -= 5;
            if (current < 0) current = 0; // prevent negative
            inputValue.setText(String.valueOf(current));
        });

        btnApply.setOnClickListener(v -> {
            int value = getInputValue();
            secondsText.setText(String.valueOf(value));
            editSection.setVisibility(View.GONE);

            // Send value to Arduino
            sendIntervalToArduino(value);

            // Save current interval to Firebase
            intervalRef.child("current").setValue(value);
        });

        btnBack.setOnClickListener(View -> {
            Intent intent = new Intent(ArduinoInterval.this, Homepage.class);
            startActivity(intent);
        });
    }

    private void sendIntervalToArduino(int interval) {
        String url = "http://" + ESP_IP + "/setInterval?value=" + interval;

        new Thread(() -> {
            try {
                java.net.URL requestUrl = new java.net.URL(url);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                int responseCode = connection.getResponseCode();
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private int getInputValue() {
        String text = inputValue.getText().toString().trim();
        if (text.isEmpty()) return 0;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
