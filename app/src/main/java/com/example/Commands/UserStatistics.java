package com.example.Commands;

import java.util.HashMap;
import java.util.Map;

public class UserStatistics {
    private String creationdate;
    private int totalsearches;
    private Map<String, Integer> searcheddocs = new HashMap<>();

    public UserStatistics() {}

    public UserStatistics(String newcreationdate, int newtotalsearches, Map<String, Integer> newsearcheddocs) {
        creationdate = newcreationdate;
        totalsearches = newtotalsearches;
        searcheddocs = newsearcheddocs;
    }

    public void setCreationDate(String newcreationdate) {
        creationdate = newcreationdate;
    }

    public void setTotalSearches(int newsearches) { 
        totalsearches = newsearches; 
    }

    public void setDocumentSearches(Map<String, Integer> newsearcheddocs) {
        searcheddocs = newsearcheddocs;
    }

    public String getCreationDate() {
        return creationdate;
    }

    public int getTotalSearches() { 
        return totalsearches; 
    }

    public Map<String, Integer> getDocumentSearches() {
        return searcheddocs;
    }
}
