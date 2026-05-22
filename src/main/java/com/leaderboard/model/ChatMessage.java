package com.leaderboard.model;

public class ChatMessage {
    private final String uniqueId;
    private final String nickname;
    private final String comment;
    private final String avatarUrl;

    public ChatMessage(String uniqueId, String nickname, String comment, String avatarUrl) {
        this.uniqueId = uniqueId;
        this.nickname = nickname;
        this.comment = comment;
        this.avatarUrl = avatarUrl;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getComment() {
        return comment;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
