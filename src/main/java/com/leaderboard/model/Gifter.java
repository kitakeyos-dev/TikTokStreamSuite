package com.leaderboard.model;

/**
 * Model representing a Gifter who has sent gifts during the stream.
 * Inherits common identity fields from BaseUser.
 */
public class Gifter extends BaseUser implements Comparable<Gifter> {
    private int points; // Accumulated points (diamonds)
    private transient int rank; // Cached transient rank for display

    public Gifter(String uniqueId, String nickname, String avatarUrl, int points) {
        super(uniqueId, nickname, avatarUrl);
        this.points = points;
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
}
