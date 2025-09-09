package com.example.plantcare;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    EditText editEmail, editPassword, editContact, editFirstName, editLastName, editUserName;
    Button btnRegister, loginTab;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editUserName = findViewById(R.id.editUserName);
        editContact = findViewById(R.id.editContact);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnRegister = findViewById(R.id.btnRegister);
        loginTab = findViewById(R.id.loginTab);
        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(v -> {
            String fname = editFirstName.getText().toString().trim();
            String lname = editLastName.getText().toString().trim();
            String username = editUserName.getText().toString().trim();
            String contact = editContact.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (TextUtils.isEmpty(fname)) {
                editFirstName.setError("First Name is required");
                return;
            }
            if (TextUtils.isEmpty(lname)) {
                editLastName.setError("Last Name is required");
                return;
            }
            if (TextUtils.isEmpty(username)) {
                editUserName.setError("Username is required");
                return;
            }
            if (TextUtils.isEmpty(contact)) {
                editContact.setError("Contact is required");
                return;
            }
            if (TextUtils.isEmpty(email)) {
                editEmail.setError("Email is required");
                return;
            }
            if (TextUtils.isEmpty(password) || password.length() < 6) {
                editPassword.setError("Password must be at least 6 characters");
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();

                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("first_name", fname);
                            userMap.put("last_name", lname);
                            userMap.put("username", username);
                            userMap.put("contact", contact);
                            userMap.put("email", email);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(uid)
                                    .setValue(userMap)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(Register.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                            Log.d("Register", "User data saved successfully");
                                        } else {
                                            Toast.makeText(Register.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                            Log.e("Register", "Error saving user data", dbTask.getException());
                                        }
                                    });
                        } else {
                            Toast.makeText(Register.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        loginTab.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
            finish(); // Optional: closes the current activity
        });
    }
}