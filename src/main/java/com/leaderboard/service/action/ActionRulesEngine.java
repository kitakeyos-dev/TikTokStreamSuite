package com.leaderboard.service.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.leaderboard.model.action.ActionType;
import com.leaderboard.model.action.StreamRule;
import com.leaderboard.model.action.TriggerType;
import com.leaderboard.service.TTSService;
import com.leaderboard.util.ConfigManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rules Engine for managing and executing custom Actions & Events.
 */
public class ActionRulesEngine {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<StreamRule> rulesList = Collections.synchronizedList(new ArrayList<>());

    static {
        load();
    }

    public static synchronized void load() {
        File file = ConfigManager.getStorageFile("actions_events.json");
        if (!file.exists()) {
            rulesList.clear();
            save();
            return;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            List<StreamRule> loaded = GSON.fromJson(reader, new TypeToken<List<StreamRule>>(){}.getType());
            rulesList.clear();
            if (loaded != null) {
                rulesList.addAll(loaded);
            }
        } catch (Exception e) {
            System.err.println("Error reading actions_events.json: " + e.getMessage());
        }
    }

    public static synchronized void save() {
        File file = ConfigManager.getStorageFile("actions_events.json");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(rulesList, writer);
        } catch (Exception e) {
            System.err.println("Error saving actions_events.json: " + e.getMessage());
        }
    }

    public static List<StreamRule> getRules() {
        return rulesList;
    }

    public static void addRule(StreamRule rule) {
        if (rule != null) {
            rulesList.add(rule);
            save();
        }
    }

    public static void deleteRule(String id) {
        if (id != null) {
            rulesList.removeIf(r -> r.getId().equals(id));
            save();
        }
    }

    // --- Core Real-time Event Handlers ---

    public static void handleComment(String userId, String nickname, String comment) {
        if (comment == null) return;
        String trimmed = comment.trim();
        
        synchronized (rulesList) {
            for (StreamRule rule : rulesList) {
                if (rule.isEnabled() && rule.getTriggerType() == TriggerType.CHAT) {
                    String cmd = rule.getTriggerTarget();
                    if (cmd != null && !cmd.trim().isEmpty()) {
                        // Match if the comment starts with the configured command (e.g. !discord)
                        if (trimmed.toLowerCase().startsWith(cmd.trim().toLowerCase())) {
                            triggerRule(rule, userId, nickname, trimmed);
                        }
                    }
                }
            }
        }
    }

    public static void handleGift(String userId, String nickname, String giftName, int diamondCost) {
        if (giftName == null) return;
        
        synchronized (rulesList) {
            for (StreamRule rule : rulesList) {
                if (rule.isEnabled() && rule.getTriggerType() == TriggerType.GIFT) {
                    String targetGift = rule.getTriggerTarget();
                    // Match if triggerTarget is empty (all gifts) OR matches the specific gift name
                    if (targetGift == null || targetGift.trim().isEmpty() || targetGift.trim().equalsIgnoreCase(giftName.trim())) {
                        triggerRule(rule, userId, nickname, giftName + " (x" + diamondCost + " xu)");
                    }
                }
            }
        }
    }

    public static void handleFollow(String userId, String nickname) {
        synchronized (rulesList) {
            for (StreamRule rule : rulesList) {
                if (rule.isEnabled() && rule.getTriggerType() == TriggerType.FOLLOW) {
                    triggerRule(rule, userId, nickname, "Follower");
                }
            }
        }
    }

    public static void handleShare(String userId, String nickname) {
        synchronized (rulesList) {
            for (StreamRule rule : rulesList) {
                if (rule.isEnabled() && rule.getTriggerType() == TriggerType.SHARE) {
                    triggerRule(rule, userId, nickname, "Share");
                }
            }
        }
    }

    public static void handleSubscribe(String userId, String nickname) {
        synchronized (rulesList) {
            for (StreamRule rule : rulesList) {
                if (rule.isEnabled() && rule.getTriggerType() == TriggerType.SUBSCRIBE) {
                    triggerRule(rule, userId, nickname, "Subscriber");
                }
            }
        }
    }

    /**
     * Executes the reactive action defined in the rule.
     */
    public static void triggerRule(StreamRule rule, String userId, String nickname, String rawPayload) {
        if (rule == null) return;
        
        String name = (nickname != null && !nickname.trim().isEmpty()) ? nickname.trim() : userId;
        String payload = (rawPayload != null) ? rawPayload : "";
        String target = (rule.getTriggerTarget() != null) ? rule.getTriggerTarget() : "";

        if (rule.getActionType() == ActionType.SOUND) {
            // Asynchronously plays local MP3 sound alert via JLayer through user selected Mixer
            double volume = ConfigManager.getConfig().getTtsVolume();
            TTSService.playLocalMP3(rule.getActionPayload(), volume);
        } else if (rule.getActionType() == ActionType.TTS) {
            // Parse custom template message replacing placeholders
            String speakText = rule.getActionPayload();
            if (speakText != null) {
                speakText = speakText
                        .replace("%nickname%", name)
                        .replace("%target%", target)
                        .replace("%payload%", payload);
                TTSService.enqueueAlertTTS(speakText);
            }
        }
    }
}
