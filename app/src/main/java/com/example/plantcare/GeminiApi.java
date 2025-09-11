package com.example.plantcare;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface GeminiApi {
    @Headers({
            "Content-Type: application/json",
            "X-goog-api-key: AIzaSyDC0-UK54iyo0x4pMlS9WDo-dbDTDFWn8A"
    })
    @POST("v1beta/models/gemini-2.0-flash:generateContent") // fixed model name
    Call<GeminiResponse> getChatResponse(@Body GeminiRequest request);
}

