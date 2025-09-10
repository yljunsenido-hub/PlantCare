package com.example.plantcare;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HumidityPage extends AppCompatActivity {

    TextView humidTxtView;
    ImageButton deleteBtn,btnBack;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private LinearLayout historyLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_humidity_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        humidTxtView = findViewById(R.id.humidTxtView);
        deleteBtn = findViewById(R.id.deleteBtn);
        btnBack = findViewById(R.id.btnBack);
        historyLayout = findViewById(R.id.historyLayout);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            String uid = user.getUid();
            userRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(uid)
                    .child("arduinoData");

            deleteBtn.setOnClickListener(v ->{
                new AlertDialog.Builder(HumidityPage.this)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete ALL history?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            userRef.removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firebase", "All history deleted");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firebase", "Delete failed", e);
                                    });
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            });

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    historyLayout.removeAllViews(); // clear old rows every time

                    if (snapshot.exists()) {
                        Integer latestHumid = null;
                        Long latestTimestamp = null;

                        for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                            Integer humid = entrySnapshot.child("humidity").getValue(Integer.class);
                            Long timestamp = entrySnapshot.child("timestamp").getValue(Long.class);

                            if (humid != null && timestamp != null) {
                                Date date = new Date(timestamp);
                                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault());
                                String formattedDate = sdf.format(date);

                                latestHumid = humid;
                                latestTimestamp = timestamp;

                                LinearLayout row = new LinearLayout(HumidityPage.this);
                                row.setOrientation(LinearLayout.HORIZONTAL);

                                TextView humidView = new TextView(HumidityPage.this);
                                humidView.setText("Humid: " + humid);
                                humidView.setTextSize(20);
                                humidView.setPadding(0, 0, 0, 15);
                                humidView.setTextColor(getResources().getColor(android.R.color.black));
                                humidView.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
                                humidView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                                TextView timeView = new TextView(HumidityPage.this);
                                timeView.setText("Date: " + formattedDate);
                                timeView.setTextSize(20);
                                timeView.setPadding(0, 0, 0, 15);
                                timeView.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
                                timeView.setTextColor(getResources().getColor(android.R.color.black));

                                row.addView(humidView);
                                row.addView(timeView);

                                historyLayout.addView(row);
                            }
                        }

                        if (latestHumid != null) {
                            humidTxtView.setText(String.valueOf(latestHumid));
                        }
                        } else {
                            // If no data exists after deletion, clear everything
                            humidTxtView.setText("No Data");
                            historyLayout.removeAllViews();
                        }
                    }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Error: " + error.getMessage());
                }
            });

        }
        btnBack.setOnClickListener(View -> {
            Intent intent = new Intent(HumidityPage.this, Homepage.class);
            startActivity(intent);
        });
    }
}