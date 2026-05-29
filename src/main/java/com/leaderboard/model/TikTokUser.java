package com.leaderboard.model;

import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.data.models.badges.Badge;
import io.github.jwdeveloper.tiktok.data.models.badges.CombineBadge;
import io.github.jwdeveloper.tiktok.data.models.badges.TextBadge;
import io.github.jwdeveloper.tiktok.data.models.badges.StringBadge;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clean POJO representing a TikTok user's data parsed from SDK models.
 * Completely isolates domain state from socket connection classes.
 */
public class TikTokUser {
    private final String uniqueId;
    private final String nickname;
    private final String avatarUrl;
    private final String teamName;
    private final int teamLevel;
    private final int giftGiverLevel;
    private final boolean isSubscriber;
    private final List<String> badgeUrls;

    public TikTokUser(String uniqueId, String nickname, String avatarUrl,
                      String teamName, int teamLevel, int giftGiverLevel,
                      boolean isSubscriber, List<String> badgeUrls) {
        this.uniqueId = uniqueId;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.teamName = teamName;
        this.teamLevel = teamLevel;
        this.giftGiverLevel = giftGiverLevel;
        this.isSubscriber = isSubscriber;
        this.badgeUrls = badgeUrls != null ? badgeUrls : new ArrayList<>();
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getTeamName() {
        return teamName;
    }

    public int getTeamLevel() {
        return teamLevel;
    }

    public int getGiftGiverLevel() {
        return giftGiverLevel;
    }

    public boolean isSubscriber() {
        return isSubscriber;
    }

    public List<String> getBadgeUrls() {
        return badgeUrls;
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
                // Extract remote badge URLs
                if (badge instanceof CombineBadge cb) {
                    if (cb.getPicture() != null) {
                        String link = cb.getPicture().getLink();
                        if (link != null && !link.isEmpty()) {
                            badgeUrls.add(link);
                        }
                    }

                    // Extract subscriber badge or fans badge details
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
