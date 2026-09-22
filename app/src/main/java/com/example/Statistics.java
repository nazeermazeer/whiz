package com.example;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Statistics {
    public static void main(String[] args) {
        File file = new File("statistics.json");
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        GameStatistics stats = new GameStatistics(
            150, 
            64.5, 
            Map.of("Player1", 2500, "Player2", 1850)
        );

        try {
            mapper.writeValue(file, stats);
            System.out.println("Statistics saved successfully!");
        } catch (IOException e) {
            System.err.println("Error saving statistics: " + e.getMessage());
        }

        try {
            if (file.exists()) {
                GameStatistics loadedStats = mapper.readValue(file, GameStatistics.class);
                System.out.println("Loaded Games Played: " + loadedStats.getGamesPlayed());
            }
        } catch (IOException e) {
            System.err.println("Error loading statistics: " + e.getMessage());
        }
    }
}
