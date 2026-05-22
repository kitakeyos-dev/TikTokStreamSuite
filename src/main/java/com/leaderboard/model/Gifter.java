package com.leaderboard.model;

import java.util.Objects;

public class Gifter implements Comparable<Gifter> {
    private String uniqueId; // Unique TikTok identifier
    private String nickname; // Nickname displayed
    private String avatarUrl; // Avatar URL
    private int points; // Accumulated points (diamonds)
    private transient int rank; // Cached transient rank for display

    public Gifter(String uniqueId, String nickname, String avatarUrl, int points) {
        this.uniqueId = uniqueId;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.points = points;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getNickname() {
        return nickname != null && !nickname.trim().isEmpty() ? nickname : uniqueId;
    }


    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void addPoints(int value) {
        this.points += value;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public int compareTo(Gifter o) {
        // Sort descending by points, fallback to nickname alphabetically
        int diff = Integer.compare(o.points, this.points);
        if (diff != 0) {
            return diff;
        }
        return this.getNickname().compareToIgnoreCase(o.getNickname());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gifter gifter = (Gifter) o;
        return Objects.equals(uniqueId, gifter.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
}
