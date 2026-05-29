package com.leaderboard.model;

/**
 * Model representing a member of the active Fan Club team.
 * Inherits common identity fields from BaseUser.
 */
public class TeamMember extends BaseUser implements Comparable<TeamMember> {
    private String teamName;      // Name of the Fan Club (e.g. Gia đình)
    private int teamLevel;        // Fan Club / Tim Đội level (1-40)
    private int giftGiverLevel;   // Gifting / Cấp độ xanh level (1-50+)
    private boolean isSubscriber; // Is user a Subscriber
    private long lastActive;      // Timestamp of last interaction

    public TeamMember(String uniqueId, String nickname, String avatarUrl, 
                      String teamName, int teamLevel, int giftGiverLevel, 
                      boolean isSubscriber, long lastActive) {
        super(uniqueId, nickname, avatarUrl);
        this.teamName = teamName;
        this.teamLevel = teamLevel;
        this.giftGiverLevel = giftGiverLevel;
        this.isSubscriber = isSubscriber;
        this.lastActive = lastActive;
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
}
