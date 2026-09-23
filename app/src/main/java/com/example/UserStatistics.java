package com.example;

public class UserStatistics {
    private int searches;

    public UserStatistics() {}

    public UserStatistics(int newsearches) {
        this.searches = newsearches;
    }

    public void setSearches(int newsearches) { 
        this.searches = newsearches; 
    }

    public int getSearches() { 
        return this.searches; 
    }
}
