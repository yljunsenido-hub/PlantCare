package com.example.plantcare;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

public class StoreCreate extends AppCompatActivity {

    EditText course, year;
    Button add;
    TextView viewCourse, viewYear;

    FirebaseAuth mAuth;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_store_create);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        course = findViewById(R.id.editCourse);
        year = findViewById(R.id.editYear);
        add = findViewById(R.id.btnAdd);
        viewCourse = findViewById(R.id.textView);
        viewYear = findViewById(R.id.textView2);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            dbRef = FirebaseDatabase.getInstance().getReference("Courses")
                    .child(user.getUid())
                    .child("courses");
        }

        // Save data
        add.setOnClickListener(v -> {
            String courseText = course.getText().toString().trim();
            String yearText = year.getText().toString().trim();

            if (TextUtils.isEmpty(courseText) || TextUtils.isEmpty(yearText)) {
                Toast.makeText(this, "Enter both course and year", Toast.LENGTH_SHORT).show();
                return;
            }

            String key = dbRef.push().getKey(); // unique key for each entry
            dbRef.child(key).child("course").setValue(courseText);
            dbRef.child(key).child("year").setValue(yearText);

            course.setText("");
            year.setText("");
            Toast.makeText(this, "Added successfully", Toast.LENGTH_SHORT).show();
        });

        // Fetch and display data
        if (dbRef != null) {
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    StringBuilder coursesBuilder = new StringBuilder();
                    StringBuilder yearsBuilder = new StringBuilder();

                    for (DataSnapshot child : snapshot.getChildren()) {
                        String c = child.child("course").getValue(String.class);
                        String y = child.child("year").getValue(String.class);

                        if (c != null) coursesBuilder.append(c).append("\n");
                        if (y != null) yearsBuilder.append(y).append("\n");
                    }

                    viewCourse.setText(coursesBuilder.toString());
                    viewYear.setText(yearsBuilder.toString());
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(StoreCreate.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
