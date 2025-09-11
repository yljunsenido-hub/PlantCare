package com.example.plantcare;

import java.util.Collections;
import java.util.List;

public class GeminiRequest {
    public List<Content> contents;

    public GeminiRequest(String text) {
        this.contents = Collections.singletonList(
                new Content(Collections.singletonList(
                        new Part("You are PlantCare Assistant. " +
                                "You only answer questions related to plant care, " +
                                "gardening, soil, moisture, humidity, and my PlantCare app. " +
                                "If a question is outside PlantCare, politely say you cannot answer." +
                                "If a question is about the average readings of the sensors, you should provide them a suggestions." +
                                "\nUser: " + text)
                ))
        );
    }

    static class Content {
        public List<Part> parts;
        public Content(List<Part> parts) { this.parts = parts; }
    }

    static class Part {
        public String text;
        public Part(String text) { this.text = text; }
    }
}
