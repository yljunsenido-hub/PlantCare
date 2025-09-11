package com.example.plantcare;

import java.util.Collections;
import java.util.List;

public class GeminiRequest {
    public List<Content> contents;

    public GeminiRequest(String text) {
        this.contents = Collections.singletonList(new Content(Collections.singletonList(new Part(text))));
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
