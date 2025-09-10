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

public class SoilPage extends AppCompatActivity {

    ImageButton btnBack, deleteBtn;
    TextView soilTxtView;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private LinearLayout historyLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_soil_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack = findViewById(R.id.btnBack);
        deleteBtn = findViewById(R.id.deleteBtn);
        soilTxtView = findViewById(R.id.soilTxtView);
        historyLayout = findViewById(R.id.historyLayout);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            userRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(uid)
                    .child("arduinoData");

            // Deleting all of the data history
            deleteBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(SoilPage.this)
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
                        Integer latestSoil = null;
                        Long latestTimestamp = null;

                        for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                            Integer soil = entrySnapshot.child("soil").getValue(Integer.class);
                            Long timestamp = entrySnapshot.child("timestamp").getValue(Long.class);

                            if (soil != null && timestamp != null) {
                                Date date = new Date(timestamp);
                                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault());
                                String formattedDate = sdf.format(date);

                                latestSoil = soil; // this keeps updating to last item
                                latestTimestamp = timestamp;

                                LinearLayout row = new LinearLayout(SoilPage.this);
                                row.setOrientation(LinearLayout.HORIZONTAL);

                                TextView soilView = new TextView(SoilPage.this);
                                soilView.setText("Soil: " + soil);
                                soilView.setTextSize(20);
                                soilView.setPadding(0, 0, 0, 15);
                                soilView.setTextColor(getResources().getColor(android.R.color.black));
                                soilView.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
                                soilView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                                TextView timeView = new TextView(SoilPage.this);
                                timeView.setText("Date: " + formattedDate);
                                timeView.setTextSize(20);
                                timeView.setPadding(0, 0, 0, 15);
                                timeView.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
                                timeView.setTextColor(getResources().getColor(android.R.color.black));

                                row.addView(soilView);
                                row.addView(timeView);

                                historyLayout.addView(row);
                            }
                        }

                        if (latestSoil != null) {
                            // ✅ Fix: convert int to String
                            soilTxtView.setText(String.valueOf(latestSoil));
                        }
                    } else {
                        soilTxtView.setText("No Data");
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
            Intent intent = new Intent(SoilPage.this, Homepage.class);
            startActivity(intent);
        });
    }
}