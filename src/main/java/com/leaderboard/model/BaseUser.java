package com.leaderboard.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class representing a generic TikTok participant/user.
 * Encapsulates common identification fields to avoid duplication (DRY)
 * while maintaining flat JSON serialization compatibility.
 */
public abstract class BaseUser {
    protected String uniqueId; // Unique TikTok identifier
    protected String nickname; // Nickname displayed
    protected String avatarUrl; // Avatar URL
    protected List<String> badgeUrls = new ArrayList<>();

    public BaseUser(String uniqueId, String nickname, String avatarUrl) {
        this.uniqueId = uniqueId;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
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

    public List<String> getBadgeUrls() {
        if (badgeUrls == null) {
            badgeUrls = new ArrayList<>();
        }
        return badgeUrls;
    }

    public void setBadgeUrls(List<String> badgeUrls) {
        this.badgeUrls = badgeUrls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseUser baseUser = (BaseUser) o;
        return Objects.equals(uniqueId, baseUser.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
}
