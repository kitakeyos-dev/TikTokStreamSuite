package com.leaderboard.model;

public record ChatMessage(String uniqueId, String nickname, String comment, String avatarUrl) {
}
