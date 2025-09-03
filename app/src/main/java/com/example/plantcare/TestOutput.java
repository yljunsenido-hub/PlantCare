package com.example.plantcare;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TestOutput extends AppCompatActivity {

    private TextView soilText, tempText, humText;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_output);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        soilText = findViewById(R.id.soilText);
        tempText = findViewById(R.id.tempText);
        humText = findViewById(R.id.humText);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();

            // ✅ Reference to this user's Arduino data
            userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .child("arduinoData");

            // Listen for data changes
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Integer soil = snapshot.child("soil").getValue(Integer.class);
                        Float temp = snapshot.child("temperature").getValue(Float.class);
                        Float hum = snapshot.child("humidity").getValue(Float.class);

                        if (soil != null) soilText.setText("Soil: " + soil);
                        if (temp != null) tempText.setText("Temp: " + temp + " °C");
                        if (hum != null) humText.setText("Humidity: " + hum + " %");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Error: " + error.getMessage());
                }
            });
        }
    }
}