package com.example.plantcare;

import android.os.Bundle;
import android.view.View; // Import View for OnClickListener
import android.widget.Button;
import android.widget.Toast; // Import Toast

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Import Volley classes
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class WifiManager extends AppCompatActivity {

    Button resetBtn;
    // Define the ESP IP Address as a constant
    // It's good practice to make this easily changeable if needed
    // private static final String ESP_IP_ADDRESS = "192.168.1.59"; // Replace with your actual ESP IP

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

//        resetBtn = findViewById(R.id.resetBtn);
//
//        // Set an OnClickListener for the reset button
//        resetBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendWifiResetRequest();
//            }
//        });
//    }
//
//    private void sendWifiResetRequest() {
//        // The URL to send the GET request to
//        String url = "http://" + ESP_IP_ADDRESS + "/resetwifi";
//
//        // Instantiate the RequestQueue.
//        RequestQueue queue = Volley.newRequestQueue(this);
//
//        // Request a string response from the provided URL.
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        // You can also parse the 'response' string if your ESP sends back data
//                        Toast.makeText(WifiManager.this, "ESP WiFi Reset Successful", Toast.LENGTH_SHORT).show();
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // You can log the error for more details: error.printStackTrace();
//                        Toast.makeText(WifiManager.this, "Failed to connect to ESP: " + error.toString(), Toast.LENGTH_LONG).show();
//                    }
//                });
//
//        // Add the request to the RequestQueue.
//        queue.add(stringRequest);
    }
}
