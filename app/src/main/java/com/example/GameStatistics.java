package com.example;

import java.util.Map;

public class GameStatistics {
    private int gamesPlayed;
    private double winRate;
    private Map<String, Integer> highScores;

    public GameStatistics() {}

    public GameStatistics(int gamesPlayed, double winRate, Map<String, Integer> highScores) {
        this.gamesPlayed = gamesPlayed;
        this.winRate = winRate;
        this.highScores = highScores;
    }

    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }
    public void setWinRate(double winRate) { this.winRate = winRate; }
    public void setHighScores(Map<String, Integer> highScores) { this.highScores = highScores; }

    public int getGamesPlayed() { return gamesPlayed; }
    public double getWinRate() { return winRate; }
    public Map<String, Integer> getHighScores() { return highScores; }
}
