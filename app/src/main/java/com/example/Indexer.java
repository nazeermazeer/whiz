package com.example;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NestedJsonExtractor {

    public static void main(String[] args) {
        // Path to your nested JSON file
        File jsonFile = new File("app/src/main/java/com/example/functions.json");
        Map<String, String> glossary = new HashMap<>();

        try {
            JsonFactory factory = new ObjectMapper().getFactory();
            try (JsonParser parser = factory.createParser(jsonFile)) {
                extractTerms(parser, glossary);
            }

            // Print the indexed results
            glossary.forEach((term, definition) -> 
                System.out.println("Term: " + term + " | Definition: " + definition)
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void extractTerms(JsonParser parser, Map<String, String> glossary) throws IOException {
        String currentTerm = null;

        while (parser.nextToken() != null) {
            JsonToken currentToken = parser.getCurrentToken();

            if (currentToken == JsonToken.FIELD_NAME) {
                String fieldName = parser.getCurrentName();

                if ("term".equals(fieldName)) {
                    parser.nextToken();
                    currentTerm = parser.getText();
                } else if ("definition".equals(fieldName) && currentTerm != null) {
                    parser.nextToken();
                    glossary.put(currentTerm, parser.getText());
                    currentTerm = null; // Reset for the next pair
                }
            } else if (currentToken == JsonToken.START_OBJECT || currentToken == JsonToken.START_ARRAY) {
                // Recursively pass the parser into nested objects/arrays
                extractTerms(parser, glossary);
            }
        }
    }
}
