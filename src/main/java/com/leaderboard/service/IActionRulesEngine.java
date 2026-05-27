package com.leaderboard.service;

import com.leaderboard.model.action.StreamRule;
import java.util.List;

/**
 * Interface representing reactive Custom Actions & Events engine.
 */
public interface IActionRulesEngine {
    void load();
    void save();
    List<StreamRule> getRules();
    void addRule(StreamRule rule);
    void deleteRule(String id);

    void handleComment(String userId, String nickname, String comment);
    void handleGift(String userId, String nickname, String giftName, int diamondCost);
    void handleFollow(String userId, String nickname);
    void handleShare(String userId, String nickname);
    void handleSubscribe(String userId, String nickname);
    void triggerRule(StreamRule rule, String userId, String nickname, String rawPayload);
}
