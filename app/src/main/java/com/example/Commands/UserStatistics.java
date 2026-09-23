package com.example.Commands;

import java.util.HashMap;
import java.util.Map;

public class UserStatistics {
    private int totalsearches;
    private Map<String, Integer> searcheddocs = new HashMap<>();

    public UserStatistics() {}

    public UserStatistics(int newsearches) {
        totalsearches = newsearches;
    }

    public void setTotalSearches(int newsearches) { 
        totalsearches = newsearches; 
    }

    public void setDocumentSearches(Map<String, Integer> newsearcheddocs) {
        searcheddocs = newsearcheddocs;
    }

    public int getTotalSearches() { 
        return totalsearches; 
    }

    public Map<String, Integer> getDocumentSearches() {
        return searcheddocs;
    }
}
