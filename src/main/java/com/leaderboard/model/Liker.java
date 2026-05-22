package com.leaderboard.model;

import java.util.Objects;

public class Liker implements Comparable<Liker> {
    private String uniqueId; // Unique TikTok identifier
    private String nickname; // Nickname displayed
    private String avatarUrl; // Avatar URL
    private int likes; // Accumulated likes
    private transient int rank; // Cached transient rank for display

    public Liker(String uniqueId, String nickname, String avatarUrl, int likes) {
        this.uniqueId = uniqueId;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.likes = likes;
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

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void addLikes(int value) {
        this.likes += value;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public int compareTo(Liker o) {
        // Sort descending by likes count, fallback to nickname alphabetically
        int diff = Integer.compare(o.likes, this.likes);
        if (diff != 0) {
            return diff;
        }
        return this.getNickname().compareToIgnoreCase(o.getNickname());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Liker liker = (Liker) o;
        return Objects.equals(uniqueId, liker.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
}
