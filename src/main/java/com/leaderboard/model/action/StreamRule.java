package com.leaderboard.model.action;

import java.util.UUID;

/**
 * Data model representing a custom interaction rule (Event-Action mapping).
 */
public class StreamRule {
    private String id;
    private String name;
    private TriggerType triggerType;
    private String triggerTarget; // Tên quà hoặc lệnh chat (ví dụ: "!discord")
    private ActionType actionType;
    private String actionPayload; // Đường dẫn file nhạc hoặc tin nhắn TTS
    private boolean enabled;

    public StreamRule() {
        this.id = UUID.randomUUID().toString();
        this.enabled = true;
    }

    public StreamRule(String name, TriggerType triggerType, String triggerTarget, ActionType actionType, String actionPayload) {
        this();
        this.name = name;
        this.triggerType = triggerType;
        this.triggerTarget = triggerTarget;
        this.actionType = actionType;
        this.actionPayload = actionPayload;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TriggerType getTriggerType() { return triggerType; }
    public void setTriggerType(TriggerType triggerType) { this.triggerType = triggerType; }

    public String getTriggerTarget() { return triggerTarget; }
    public void setTriggerTarget(String triggerTarget) { this.triggerTarget = triggerTarget; }

    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }

    public String getActionPayload() { return actionPayload; }
    public void setActionPayload(String actionPayload) { this.actionPayload = actionPayload; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
