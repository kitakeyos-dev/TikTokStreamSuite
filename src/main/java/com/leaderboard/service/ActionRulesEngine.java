package com.leaderboard.service;

import com.leaderboard.model.action.StreamRule;
import com.leaderboard.service.ServiceLocator;
import java.util.List;

/**
 * Service Facade for ActionRulesEngine that routes static rules triggers and
 * management calls directly to the active IActionRulesEngine instance registered in ServiceLocator.
 * Maintains backwards compatibility across the entire codebase.
 */
public class ActionRulesEngine {
    private static IActionRulesEngine getEngine() {
        return ServiceLocator.get(IActionRulesEngine.class);
    }

    public static void load() {
        getEngine().load();
    }

    public static void save() {
        getEngine().save();
    }

    public static List<StreamRule> getRules() {
        return getEngine().getRules();
    }

    public static void addRule(StreamRule rule) {
        getEngine().addRule(rule);
    }

    public static void deleteRule(String id) {
        getEngine().deleteRule(id);
    }

    public static void handleComment(String userId, String nickname, String comment) {
        getEngine().handleComment(userId, nickname, comment);
    }

    public static void handleGift(String userId, String nickname, String giftName, int diamondCost) {
        getEngine().handleGift(userId, nickname, giftName, diamondCost);
    }

    public static void handleFollow(String userId, String nickname) {
        getEngine().handleFollow(userId, nickname);
    }

    public static void handleShare(String userId, String nickname) {
        getEngine().handleShare(userId, nickname);
    }

    public static void handleSubscribe(String userId, String nickname) {
        getEngine().handleSubscribe(userId, nickname);
    }

    public static void triggerRule(StreamRule rule, String userId, String nickname, String rawPayload) {
        getEngine().triggerRule(rule, userId, nickname, rawPayload);
    }
}
