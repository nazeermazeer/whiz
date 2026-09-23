package com.example.Commands;

import java.util.HashMap;
import java.util.Map;

public class UserStatistics {
    private int totalsearches;
    private Map<String, Integer> searcheddocs = new HashMap<>();

    public UserStatistics() {}

    public UserStatistics(int newsearches) {
        this.totalsearches = newsearches;
    }

    public void setTotalSearches(int newsearches) { 
        this.totalsearches = newsearches; 
    }

    public void setDocumentSearches(Map<String, Integer> newsearcheddocs) {
        this.searcheddocs = newsearcheddocs;
    }

    public int getTotalSearches() { 
        return this.totalsearches; 
    }

    public Map<String, Integer> getDocumentSearches() {
        return searcheddocs;
    }
}
