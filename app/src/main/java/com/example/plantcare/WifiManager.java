package com.example.plantcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class WifiManager extends AppCompatActivity {

    Button resetBtn;
    TextView wifiInfoText, statusText;
    ImageButton btnBack;
    ImageView imageView;
    private static final String ESP_IP_ADDRESS = "esp8266.local";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wifi_manager);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        resetBtn = findViewById(R.id.resetBtn);
        wifiInfoText = findViewById(R.id.wifiInfoText);
        statusText = findViewById(R.id.statusText);
        btnBack = findViewById(R.id.btnBack);
        imageView = findViewById(R.id.imageView);

        // 🔹 Set default UI
        wifiInfoText.setText("Loading WiFi info...");
        statusText.setText("Not Connected");
        statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        imageView.setBackgroundResource(R.drawable.wifioff);

        if (resetBtn != null) {
            resetBtn.setOnClickListener(v -> sendWifiResetRequest());
        }

        // 🔹 Fetch WiFi info immediately when opening the page
        fetchWifiInfo();

        btnBack.setOnClickListener(View -> {
            Intent intent = new Intent(WifiManager.this, Homepage.class);
            startActivity(intent);
        });
    }

    private void sendWifiResetRequest() {
        String url = "http://" + ESP_IP_ADDRESS + "/resetwifi";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> Toast.makeText(WifiManager.this, "ESP WiFi Reset Successful", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(WifiManager.this, "Failed to connect: " + error.toString(), Toast.LENGTH_LONG).show());

        queue.add(stringRequest);
    }

    private void fetchWifiInfo() {
        String url = "http://" + ESP_IP_ADDRESS + "/wifiinfo";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String ssid = jsonObject.optString("ssid", "");
                        String bssid = jsonObject.optString("bssid", "");
                        String local_ip = jsonObject.optString("local_ip", "");
                        String gateway = jsonObject.optString("gateway", "");
                        String subnet = jsonObject.optString("subnet", "");
                        String mac = jsonObject.optString("mac", "");

                        if (!ssid.isEmpty() && !local_ip.isEmpty()) {
                            // ✅ Connected state
                            wifiInfoText.setText(
                                    "SSID: " + ssid +
                                            "\nBSSID: " + bssid +
                                            "\nLocal IP: " + local_ip +
                                            "\nGateway: " + gateway +
                                            "\nSubnet: " + subnet +
                                            "\nMAC: " + mac
                            );

                            statusText.setText("Connected");
                            statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            imageView.setBackgroundResource(R.drawable.wifi);

                        } else {
                            wifiInfoText.setText("No WiFi info available");
                            statusText.setText("Not Connected");
                            statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            imageView.setBackgroundResource(R.drawable.wifioff);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        wifiInfoText.setText("Parse error");
                        statusText.setText("Not Connected");
                        statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        imageView.setBackgroundResource(R.drawable.wifioff);
                    }
                },
                error -> {
                    wifiInfoText.setText("Failed: " + error.toString());
                    statusText.setText("Not Connected");
                    statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    imageView.setBackgroundResource(R.drawable.wifioff);
                });

        queue.add(stringRequest);
    }
}
