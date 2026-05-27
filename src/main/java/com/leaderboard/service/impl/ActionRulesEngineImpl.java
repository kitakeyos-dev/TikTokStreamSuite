package com.leaderboard.service.impl;

import com.leaderboard.service.IActionRulesEngine;
import com.leaderboard.service.TTSService;
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
 * Implementation of IActionRulesEngine encapsulating instance variables and reactive rules execution.
 */
public class ActionRulesEngineImpl implements IActionRulesEngine {
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final List<StreamRule> rulesList = Collections.synchronizedList(new ArrayList<>());

    public ActionRulesEngineImpl() {
        load();
    }

    @Override
    public synchronized void load() {
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

    @Override
    public synchronized void save() {
        File file = ConfigManager.getStorageFile("actions_events.json");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(rulesList, writer);
        } catch (Exception e) {
            System.err.println("Error saving actions_events.json: " + e.getMessage());
        }
    }

    @Override
    public List<StreamRule> getRules() {
        return rulesList;
    }

    @Override
    public void addRule(StreamRule rule) {
        if (rule != null) {
            rulesList.add(rule);
            save();
        }
    }

    @Override
    public void deleteRule(String id) {
        if (id != null) {
            rulesList.removeIf(r -> r.getId().equals(id));
            save();
        }
    }

    @Override
    public void handleComment(String userId, String nickname, String comment) {
        if (comment == null) return;
        String trimmed = comment.trim();
        
        synchronized (rulesList) {
            for (StreamRule rule : rulesList) {
                if (rule.isEnabled() && rule.getTriggerType() == TriggerType.CHAT) {
                    String cmd = rule.getTriggerTarget();
                    if (cmd != null && !cmd.trim().isEmpty()) {
                        if (trimmed.toLowerCase().startsWith(cmd.trim().toLowerCase())) {
                            triggerRule(rule, userId, nickname, trimmed);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleGift(String userId, String nickname, String giftName, int diamondCost) {
        if (giftName == null) return;
        
        synchronized (rulesList) {
            for (StreamRule rule : rulesList) {
                if (rule.isEnabled() && rule.getTriggerType() == TriggerType.GIFT) {
                    String targetGift = rule.getTriggerTarget();
                    if (targetGift == null || targetGift.trim().isEmpty() || targetGift.trim().equalsIgnoreCase(giftName.trim())) {
                        triggerRule(rule, userId, nickname, giftName + " (x" + diamondCost + " xu)");
                    }
                }
            }
        }
    }

    @Override
    public void handleFollow(String userId, String nickname) {
        synchronized (rulesList) {
            for (StreamRule rule : rulesList) {
                if (rule.isEnabled() && rule.getTriggerType() == TriggerType.FOLLOW) {
                    triggerRule(rule, userId, nickname, "Follower");
                }
            }
        }
    }

    @Override
    public void handleShare(String userId, String nickname) {
        synchronized (rulesList) {
            for (StreamRule rule : rulesList) {
                if (rule.isEnabled() && rule.getTriggerType() == TriggerType.SHARE) {
                    triggerRule(rule, userId, nickname, "Share");
                }
            }
        }
    }

    @Override
    public void handleSubscribe(String userId, String nickname) {
        synchronized (rulesList) {
            for (StreamRule rule : rulesList) {
                if (rule.isEnabled() && rule.getTriggerType() == TriggerType.SUBSCRIBE) {
                    triggerRule(rule, userId, nickname, "Subscriber");
                }
            }
        }
    }

    @Override
    public void triggerRule(StreamRule rule, String userId, String nickname, String rawPayload) {
        if (rule == null) return;
        
        String name = (nickname != null && !nickname.trim().isEmpty()) ? nickname.trim() : userId;
        String payload = (rawPayload != null) ? rawPayload : "";
        String target = (rule.getTriggerTarget() != null) ? rule.getTriggerTarget() : "";

        if (rule.getActionType() == ActionType.SOUND) {
            double volume = ConfigManager.getConfig().getTtsVolume();
            TTSService.playLocalMP3(rule.getActionPayload(), volume);
        } else if (rule.getActionType() == ActionType.TTS) {
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
