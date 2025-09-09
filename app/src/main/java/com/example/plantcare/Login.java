package com.example.plantcare;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Login extends AppCompatActivity {

    EditText editEmail, editPassword;
    Button btnLogin,registerTab;
    FirebaseAuth mAuth;

    // Change this to your ESP’s IP (static IP recommended)
    private static final String ESP_IP = "192.168.1.59";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        registerTab = findViewById(R.id.registerTab);
        mAuth = FirebaseAuth.getInstance();

        registerTab.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                editEmail.setError("Email is required");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                editPassword.setError("Password is required");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();
                                sendUIDToArduino(uid);
                            }

                            // Move to next screen
                            Intent intent = new Intent(Login.this, Homepage.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Login.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });


    }



    private void sendUIDToArduino(String uid) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://" + ESP_IP + "/uid?uid=" + uid;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Arduino", "Error: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(Login.this, "Failed to send UID to ESP", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resp = response.body().string();
                    Log.d("Arduino", "Response: " + resp);
                    runOnUiThread(() ->
                            Toast.makeText(Login.this, "UID sent to ESP!", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    Log.e("Arduino", "Failed with code: " + response.code());
                    runOnUiThread(() ->
                            Toast.makeText(Login.this, "ESP error: " + response.code(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }


}
