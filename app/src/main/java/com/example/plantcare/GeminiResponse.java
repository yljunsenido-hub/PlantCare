package com.example.plantcare;

import java.util.List;

public class GeminiResponse {
    public List<Candidate> candidates;

    public static class Candidate {
        public Content content;  // <- was List<Content>, should be single object
    }

    public static class Content {
        public List<Part> parts;
    }

    public static class Part {
        public String text;
    }
}

