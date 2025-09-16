package com.example.plantcare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.cardview.widget.CardView;

public class Homepage extends AppCompatActivity {

    CardView profileBtn, chatbotBtn, soilBtn, tempBtn, humidityBtn, ldrBtn;
    TextView soilTxtView, tempTxtView, humidityTxtView, ldrTxtView;

    CardView func1, func2, func3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profileBtn = findViewById(R.id.profileBtn);
        chatbotBtn = findViewById(R.id.chatbotBtn);
        soilBtn = findViewById(R.id.soilBtn);
        tempBtn = findViewById(R.id.tempBtn);
        humidityBtn = findViewById(R.id.humidityBtn);
        ldrBtn = findViewById(R.id.ldrBtn);

        soilTxtView = findViewById(R.id.soilTxtView);
        tempTxtView = findViewById(R.id.tempTxtView);
        humidityTxtView = findViewById(R.id.humidityTxtView);
        ldrTxtView = findViewById(R.id.ldrTxtView);

        func1 = findViewById(R.id.func1);
        func2 = findViewById(R.id.func2);
        func3 = findViewById(R.id.func3);

        // Set click listeners for the buttons
        //Profile
        profileBtn.setOnClickListener(v -> {
            Intent profIntent = new Intent(Homepage.this, ProfilePage.class);
            startActivity(profIntent);
        });

        //Chatbot
        chatbotBtn.setOnClickListener(v -> {
            Intent chatIntent = new Intent(Homepage.this, Ai_Chatbot.class);
            startActivity(chatIntent);
        });

        //Soil
        soilBtn.setOnClickListener(v -> {
            Intent soilIntent = new Intent(Homepage.this, SoilPage.class);
            startActivity(soilIntent);
        });

        //Temperature
        tempBtn.setOnClickListener(v -> {
            Intent tempIntent = new Intent(Homepage.this, TemperaturePage.class);
            startActivity(tempIntent);
        });

        //Humidity
        humidityBtn.setOnClickListener(v -> {
            Intent humidityIntent = new Intent(Homepage.this, HumidityPage.class);
            startActivity(humidityIntent);
        });

        //LDR
        ldrBtn.setOnClickListener(v -> {
            Intent ldrIntent = new Intent(Homepage.this, Homepage.class);
            startActivity(ldrIntent);
        });

        func1.setOnClickListener(v -> {
            Intent func1Intent = new Intent(Homepage.this, WifiManager.class);
            startActivity(func1Intent);
        });

        func2.setOnClickListener(v -> {
            Intent func2Intent = new Intent(Homepage.this, ArduinoInterval.class);
            startActivity(func2Intent);
        });

        ldrBtn.setOnClickListener(v -> {
            Intent ldrIntent = new Intent(Homepage.this, Homepage.class);
            startActivity(ldrIntent);
        });

    }
}