package com.example;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Statistics {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private final File file = new File("statistics.json");
    private UserStatistics stats;

    public void loadStatistics() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.stats = mapper.readValue(file, UserStatistics.class);
        logger.info("loaded user statistics");
    }

    public void increaseSearches() {
        int searches = this.stats.getSearches();
        this.stats.setSearches(searches + 1);
    }

    public void writeStatistics() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(file, stats);
        logger.info("statistics saved successfully");

    }
}
