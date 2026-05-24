package com.leaderboard.model;

import java.util.Objects;

public class TeamMember implements Comparable<TeamMember> {
    private String uniqueId;      // TikTok ID (e.g. @user123)
    private String nickname;      // Display nickname
    private String avatarUrl;     // User's avatar URL
    private String teamName;      // Name of the Fan Club (e.g. Gia đình)
    private int teamLevel;        // Fan Club / Tim Đội level (1-40)
    private int giftGiverLevel;   // Gifting / Cấp độ xanh level (1-50+)
    private boolean isSubscriber; // Is user a Subscriber
    private long lastActive;      // Timestamp of last interaction

    public TeamMember(String uniqueId, String nickname, String avatarUrl, 
                      String teamName, int teamLevel, int giftGiverLevel, 
                      boolean isSubscriber, long lastActive) {
        this.uniqueId = uniqueId;
        this.nickname = nickname != null && !nickname.trim().isEmpty() ? nickname : uniqueId;
        this.avatarUrl = avatarUrl;
        this.teamName = teamName;
        this.teamLevel = teamLevel;
        this.giftGiverLevel = giftGiverLevel;
        this.isSubscriber = isSubscriber;
        this.lastActive = lastActive;
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

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public int getTeamLevel() {
        return teamLevel;
    }

    public void setTeamLevel(int teamLevel) {
        this.teamLevel = teamLevel;
    }

    public int getGiftGiverLevel() {
        return giftGiverLevel;
    }

    public void setGiftGiverLevel(int giftGiverLevel) {
        this.giftGiverLevel = giftGiverLevel;
    }

    public boolean isSubscriber() {
        return isSubscriber;
    }

    public void setSubscriber(boolean subscriber) {
        isSubscriber = subscriber;
    }

    public long getLastActive() {
        return lastActive;
    }

    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }

    public void updateActive() {
        this.lastActive = System.currentTimeMillis();
    }

    @Override
    public int compareTo(TeamMember o) {
        // Sort descending by last active timestamp (most active first)
        return Long.compare(o.lastActive, this.lastActive);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamMember that = (TeamMember) o;
        return Objects.equals(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
}
