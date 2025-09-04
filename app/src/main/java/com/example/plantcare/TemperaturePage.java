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

public class TemperaturePage extends AppCompatActivity {

    ImageButton btnBack, deleteBtn;
    TextView tempTxtView;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private LinearLayout historyLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_temperature_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack = findViewById(R.id.btnBack);
        tempTxtView = findViewById(R.id.tempTxtView);
        historyLayout = findViewById(R.id.historyLayout);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            String uid = user.getUid();
            userRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(uid)
                    .child("arduinoData")
                    .child("dummy");

            // Deleting all of the data history
            deleteBtn.setOnClickListener(View -> {
                new AlertDialog.Builder(TemperaturePage.this)
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
                        Integer latestTemp = null;
                        Long latestTimestamp = null;

                        for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                            Integer temp = entrySnapshot.child("temperature").getValue(Integer.class);
                            Long timestamp = entrySnapshot.child("timestamp").getValue(Long.class);

                            if (temp != null && timestamp != null) {
                                Date date = new Date(timestamp);
                                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault());
                                String formattedDate = sdf.format(date);

                                latestTemp = temp;
                                latestTimestamp = timestamp;

                                LinearLayout row = new LinearLayout(TemperaturePage.this);
                                row.setOrientation(LinearLayout.HORIZONTAL);

                                TextView tempView = new TextView(TemperaturePage.this);
                                tempView.setText("Temp: " + temp);
                                tempView.setTextSize(20);
                                tempView.setPadding(0, 0, 0, 15);
                                tempView.setTextColor(getResources().getColor(android.R.color.black));
                                tempView.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
                                tempView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                                TextView timeView = new TextView(TemperaturePage.this);
                                timeView.setText("Date: " + formattedDate);
                                timeView.setTextSize(20);
                                timeView.setPadding(0, 0, 0, 15);
                                timeView.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
                                timeView.setTextColor(getResources().getColor(android.R.color.black));

                                row.addView(tempView);
                                row.addView(timeView);

                                historyLayout.addView(row);
                            }
                        }

                        if (latestTemp != null) {
                            tempTxtView.setText("Current Temp: " + latestTemp);
                        }
                    } else {
                        // If no data exists after deletion, clear everything
                        tempTxtView.setText("Current Temp: ---");
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
            Intent intent = new Intent(TemperaturePage.this, Homepage.class);
            startActivity(intent);
        });
    }
}