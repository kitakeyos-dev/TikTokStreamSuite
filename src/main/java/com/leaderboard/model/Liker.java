package com.leaderboard.model;

/**
 * Model representing a Liker who has sent likes during the stream.
 * Inherits common identity fields from BaseUser.
 */
public class Liker extends BaseUser implements Comparable<Liker> {
    private int likes; // Accumulated likes
    private transient int rank; // Cached transient rank for display

    public Liker(String uniqueId, String nickname, String avatarUrl, int likes) {
        super(uniqueId, nickname, avatarUrl);
        this.likes = likes;
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
}
