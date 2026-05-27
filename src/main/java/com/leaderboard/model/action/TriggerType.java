package com.leaderboard.model.action;

/**
 * Supported trigger event types for custom actions in TikTokStreamSuite.
 */
public enum TriggerType {
    GIFT,       // Tặng quà TikTok
    FOLLOW,     // Theo dõi mới
    SHARE,      // Chia sẻ phòng live
    SUBSCRIBE,  // Đăng ký hội viên mới
    CHAT        // Lệnh chat đặc biệt bắt đầu bằng !
}
