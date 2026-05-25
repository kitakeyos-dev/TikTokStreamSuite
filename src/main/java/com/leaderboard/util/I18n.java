package com.leaderboard.util;

import java.util.Locale;
import java.util.ResourceBundle;

public final class I18n {
    private static ResourceBundle bundle;
    private static Locale currentLocale;

    static {
        loadBundle();
    }

    private I18n() {
    }

    public static synchronized void loadBundle() {
        String lang = ConfigManager.getConfig().getLanguage();
        currentLocale = new Locale(lang);
        try {
            bundle = ResourceBundle.getBundle("i18n.messages", currentLocale);
        } catch (Exception e) {
            System.err.println("Failed to load ResourceBundle for locale: " + currentLocale + ", falling back to Vietnamese.");
            currentLocale = new Locale("vi");
            try {
                bundle = ResourceBundle.getBundle("i18n.messages", currentLocale);
            } catch (Exception ex) {
                System.err.println("Failed to load fallback ResourceBundle: " + ex.getMessage());
                bundle = null;
            }
        }
    }

    public static String get(String key) {
        if (bundle == null) return key;
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    public static String get(String key, Object... args) {
        if (bundle == null) return key;
        try {
            String pattern = bundle.getString(key);
            return String.format(currentLocale, pattern, args);
        } catch (Exception e) {
            return key;
        }
    }

    public static Locale getLocale() {
        return currentLocale;
    }
}
