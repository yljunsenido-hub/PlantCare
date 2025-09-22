package com.example.plantcare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfilePage extends AppCompatActivity {

    TextView usernametxt, lastnametxt, firstnametxt, contacttxt, emailtxt;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    ImageButton btnBack;
    Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernametxt = findViewById(R.id.usernametxt);
        lastnametxt = findViewById(R.id.lastnametxt);
        firstnametxt = findViewById(R.id.firstnametxt);
        contacttxt = findViewById(R.id.contacttxt);
        emailtxt = findViewById(R.id.emailtxt);
        btnBack = findViewById(R.id.btnBack);
        logoutBtn = findViewById(R.id.logoutBtn);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            String uid = user.getUid();
            userRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(uid);
        }

        if(userRef != null){
            userRef.child("username").get().addOnSuccessListener(dataSnapshot -> {
                if(dataSnapshot.exists()){
                    String username = dataSnapshot.getValue(String.class);
                    usernametxt.setText(username);
                }
            });
            userRef.child("first_name").get().addOnSuccessListener(dataSnapshot -> {
                if(dataSnapshot.exists()){
                    String firstname = dataSnapshot.getValue(String.class);
                    firstnametxt.setText(firstname);
                }
            });
            userRef.child("last_name").get().addOnSuccessListener(dataSnapshot -> {
                if(dataSnapshot.exists()){
                    String lastname = dataSnapshot.getValue(String.class);
                    lastnametxt.setText(lastname);
                }
            });
            userRef.child("email").get().addOnSuccessListener(dataSnapshot -> {
                if(dataSnapshot.exists()){
                    String email = dataSnapshot.getValue(String.class);
                    emailtxt.setText(email);
                }
            });
            userRef.child("contact").get().addOnSuccessListener(dataSnapshot -> {
                if(dataSnapshot.exists()){
                    String contact = dataSnapshot.getValue(String.class);
                    contacttxt.setText(contact);
                }
            });
        }

        btnBack.setOnClickListener(View -> {
            Intent intent = new Intent(ProfilePage.this, Homepage.class);
            startActivity(intent);
        });

        logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfilePage.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
            startActivity(intent);
            finish();
        });
    }
}