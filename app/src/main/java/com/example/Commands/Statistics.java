package com.example.Commands;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.Main;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Statistics {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private final File file = new File("statistics.json");
    private UserStatistics stats;

    public void loadStatistics() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.stats = mapper.readValue(file, UserStatistics.class);
            logger.info("loaded user statistics");
        } catch (IOException err) {
            this.stats = new UserStatistics(0);
            this.writeStatistics();
            logger.warn("starting stats file from scratch due to error...", err);
        }
    }

    public void increaseTotalSearches() {
        int searches = this.stats.getTotalSearches();
        stats.setTotalSearches(searches + 1);
    }

    public int getTotalSearches() {
        return stats.getTotalSearches();
    }

    public void writeStatistics() throws IOException {
        ObjectMapper mapper = new ObjectMapper();        
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        mapper.writeValue(file, stats);
        logger.info("statistics saved successfully");

    }
}
