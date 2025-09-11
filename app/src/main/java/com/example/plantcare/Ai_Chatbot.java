package com.example.plantcare;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Ai_Chatbot extends AppCompatActivity{
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";

    private GeminiApi geminiApi;
    private EditText inputMessage;
    private ChatAdapter adapter;
    private List<ChatMessage> chatList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ai_chatbot);

        RecyclerView chatRecycler = findViewById(R.id.chatRecycler);
        inputMessage = findViewById(R.id.inputMessage);
        Button sendButton = findViewById(R.id.sendButton);

        adapter = new ChatAdapter(chatList);
        chatRecycler.setLayoutManager(new LinearLayoutManager(this));
        chatRecycler.setAdapter(adapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        geminiApi = retrofit.create(GeminiApi.class);

        sendButton.setOnClickListener(v -> {
            String message = inputMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                addMessage(new ChatMessage(message, true));
                sendMessageToGemini(message);
                inputMessage.setText("");
            }
        });
    }

    private void addMessage(ChatMessage msg) {
        chatList.add(msg);
        adapter.notifyItemInserted(chatList.size() - 1);
    }

    private void sendMessageToGemini(String message) {
        GeminiRequest request = new GeminiRequest(message);

        geminiApi.getChatResponse(request).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GeminiResponse res = response.body();

                    if (res.candidates != null && !res.candidates.isEmpty()) {
                        GeminiResponse.Candidate candidate = res.candidates.get(0);
                        if (candidate.content != null &&
                                candidate.content.parts != null &&
                                !candidate.content.parts.isEmpty()) {

                            String reply = candidate.content.parts.get(0).text;
                            addMessage(new ChatMessage(reply, false));
                            return;
                        }
                    }
                    addMessage(new ChatMessage("No reply content.", false));
                } else {
                    addMessage(new ChatMessage("Response failed: " + response.message(), false));
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                addMessage(new ChatMessage("Failed: " + t.getMessage(), false));
            }
        });
    }
}
