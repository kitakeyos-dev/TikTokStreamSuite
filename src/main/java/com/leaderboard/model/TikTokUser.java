package com.leaderboard.model;

import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.data.models.badges.Badge;
import io.github.jwdeveloper.tiktok.data.models.badges.CombineBadge;
import io.github.jwdeveloper.tiktok.data.models.badges.TextBadge;
import io.github.jwdeveloper.tiktok.data.models.badges.StringBadge;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unified Domain Entity representing a TikTok user's complete identity,
 * metrics (gift points, likes), and social status.
 * Single source of truth for the entire application.
 */
public class TikTokUser {
    private String uniqueId;
    private String nickname;
    private String avatarUrl;
    private String teamName;
    private int teamLevel;
    private int giftGiverLevel;
    private boolean isSubscriber;
    private List<String> badgeUrls = new ArrayList<>();

    // Cumulative stats & interaction state
    private int giftPoints;
    private int likesSent;
    private long lastActive;

    // Transient rank display state (not saved in database JSON)
    private transient int rank;

    // Empty constructor for GSON deserialization
    public TikTokUser() {}

    public TikTokUser(String uniqueId, String nickname, String avatarUrl) {
        this.uniqueId = uniqueId;
        this.nickname = nickname != null && !nickname.trim().isEmpty() ? nickname : uniqueId;
        this.avatarUrl = avatarUrl;
        this.lastActive = System.currentTimeMillis();
    }

    public TikTokUser(String uniqueId, String nickname, String avatarUrl,
                      String teamName, int teamLevel, int giftGiverLevel,
                      boolean isSubscriber, List<String> badgeUrls) {
        this.uniqueId = uniqueId;
        this.nickname = nickname != null && !nickname.trim().isEmpty() ? nickname : uniqueId;
        this.avatarUrl = avatarUrl;
        this.teamName = teamName;
        this.teamLevel = teamLevel;
        this.giftGiverLevel = giftGiverLevel;
        this.isSubscriber = isSubscriber;
        this.badgeUrls = badgeUrls != null ? badgeUrls : new ArrayList<>();
        this.lastActive = System.currentTimeMillis();
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

    public List<String> getBadgeUrls() {
        if (badgeUrls == null) {
            badgeUrls = new ArrayList<>();
        }
        return badgeUrls;
    }

    public void setBadgeUrls(List<String> badgeUrls) {
        this.badgeUrls = badgeUrls;
    }

    public int getGiftPoints() {
        return giftPoints;
    }

    public void setGiftPoints(int giftPoints) {
        this.giftPoints = giftPoints;
    }

    public void addGiftPoints(int points) {
        this.giftPoints += points;
    }

    public int getLikesSent() {
        return likesSent;
    }

    public void setLikesSent(int likes) {
        this.likesSent = likes;
    }

    public void addLikesSent(int likes) {
        this.likesSent += likes;
    }

    public long getLastActive() {
        return lastActive;
    }

    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TikTokUser user = (TikTokUser) o;
        return Objects.equals(uniqueId != null ? uniqueId.toLowerCase() : null,
                              user.uniqueId != null ? user.uniqueId.toLowerCase() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId != null ? uniqueId.toLowerCase() : null);
    }

    /**
     * Factory method parsing a TikTok user and their badges from raw SDK models.
     */
    public static TikTokUser fromSDK(User user) {
        if (user == null) {
            return new TikTokUser("", "", null, null, 0, 0, false, new ArrayList<>());
        }

        String uniqueId = user.getName();
        String nickname = user.getProfileName();
        
        String avatarUrl = null;
        if (user.getPicture() != null) {
            avatarUrl = user.getPicture().getLink();
        }

        boolean isSubscriber = user.isSubscriber();
        String teamName = null;
        int teamLevel = 0;
        int giftGiverLevel = 0;
        List<String> badgeUrls = new ArrayList<>();

        if (user.getBadges() != null) {
            for (Badge badge : user.getBadges()) {
                if (badge instanceof CombineBadge cb) {
                    if (cb.getPicture() != null) {
                        String link = cb.getPicture().getLink();
                        if (link != null && !link.isEmpty()) {
                            badgeUrls.add(link);
                        }
                    }

                    String picLink = (cb.getPicture() != null) ? cb.getPicture().getLink() : "";
                    if (picLink != null && picLink.contains("fans_badge_icon")) {
                        if (teamName == null) {
                            if (cb.getSubText() != null && !cb.getSubText().trim().isEmpty()) {
                                teamName = cb.getSubText().trim();
                            } else if (cb.getText() != null && !cb.getText().trim().isEmpty()) {
                                teamName = cb.getText().trim();
                            }
                        }
                        
                        if (teamLevel == 0) {
                            Matcher matcher = Pattern.compile("fans_badge_icon_lv(\\d+)").matcher(picLink);
                            if (matcher.find()) {
                                teamLevel = Integer.parseInt(matcher.group(1));
                            }
                        }
                    } else {
                        if (cb.getSubText() != null && cb.getSubText().matches("\\d+")) {
                            giftGiverLevel = Math.max(giftGiverLevel, Integer.parseInt(cb.getSubText()));
                        } else if (cb.getText() != null && cb.getText().matches("\\d+")) {
                            giftGiverLevel = Math.max(giftGiverLevel, Integer.parseInt(cb.getText()));
                        }
                    }
                } else if (badge instanceof TextBadge tb) {
                    if (tb.getText() != null && tb.getText().matches("\\d+")) {
                        giftGiverLevel = Math.max(giftGiverLevel, Integer.parseInt(tb.getText()));
                    }
                } else if (badge instanceof StringBadge sb) {
                    if (sb.getText() != null && sb.getText().matches("\\d+")) {
                        giftGiverLevel = Math.max(giftGiverLevel, Integer.parseInt(sb.getText()));
                    }
                }
            }
        }

        return new TikTokUser(uniqueId, nickname, avatarUrl, teamName, teamLevel, giftGiverLevel, isSubscriber, badgeUrls);
    }
}
