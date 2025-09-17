package com.example.plantcare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.cardview.widget.CardView;

public class Homepage extends AppCompatActivity {

    CardView profileBtn, chatbotBtn, soilBtn, tempBtn, humidityBtn, wifiBtn;
    TextView soilTxtView, tempTxtView, humidityTxtView, ldrTxtView;
    CardView func1, func2, func3;
    ImageView func3Icon;
    private static final String ESP_IP = "esp8266.local";

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
        wifiBtn = findViewById(R.id.wifiBtn);
        func3Icon = findViewById(R.id.func3Icon);

        soilTxtView = findViewById(R.id.soilTxtView);
        tempTxtView = findViewById(R.id.tempTxtView);
        humidityTxtView = findViewById(R.id.humidityTxtView);
        ldrTxtView = findViewById(R.id.ldrTxtView);

        func1 = findViewById(R.id.func1);
        func2 = findViewById(R.id.func2);
        func3 = findViewById(R.id.func3);

        func3.setOnClickListener(v -> {
            // Change icon immediately to indicate "working"
            runOnUiThread(() -> func3Icon.setImageResource(R.drawable.refreshgreen));

            new Thread(() -> {
                try {
                    String url = "http://" + ESP_IP + "/forcePush";
                    java.net.URL requestUrl = new java.net.URL(url);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) requestUrl.openConnection();
                    conn.setRequestMethod("GET");
                    conn.connect();
                    conn.getInputStream().close();

                    // After request finishes, restore original image
                    runOnUiThread(() -> func3Icon.setImageResource(R.drawable.refresh));

                } catch (Exception e) {
                    e.printStackTrace();
                    // In case of error, also reset to default
                    runOnUiThread(() -> func3Icon.setImageResource(R.drawable.refresh));
                }
            }).start();
        });


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
        wifiBtn.setOnClickListener(v -> {
            Intent wifiIntent = new Intent(Homepage.this, WifiManager.class);
            startActivity(wifiIntent);
        });

        func1.setOnClickListener(v -> {
            Intent func1Intent = new Intent(Homepage.this, ThresholdAlert.class);
            startActivity(func1Intent);
        });

        func2.setOnClickListener(v -> {
            Intent func2Intent = new Intent(Homepage.this, ArduinoInterval.class);
            startActivity(func2Intent);
        });
    }
}