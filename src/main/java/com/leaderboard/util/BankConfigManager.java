package com.leaderboard.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Enumeration;

/**
 * Encrypted bank credentials and API settings, stored alongside other app data.
 */
public class BankConfigManager {
    private static final Gson GSON = new GsonBuilder().create();
    private static final String APP_SALT = "TikTokStreamSuite-Bank-v1";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    public static class BankSettings {
        private String username = "";
        private String password = "";
        private String accountNumber = "";
        @SerializedName("intervalMinutes")
        private int pollIntervalSeconds = 30;
        private String apiUrl = "https://donate.nhimnhor.com";
        private String apiKey = "";

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

        public int getPollIntervalSeconds() { return pollIntervalSeconds; }
        public void setPollIntervalSeconds(int pollIntervalSeconds) { this.pollIntervalSeconds = pollIntervalSeconds; }

        public String getApiUrl() { return apiUrl; }
        public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    }

    private static BankSettings currentSettings = new BankSettings();

    static {
        load();
    }

    public static synchronized BankSettings getSettings() {
        return currentSettings;
    }

    public static synchronized void load() {
        File file = ConfigManager.getStorageFile("bank_config.dat");
        if (!file.exists()) {
            currentSettings = new BankSettings();
            return;
        }

        try {
            byte[] encrypted = Files.readAllBytes(file.toPath());
            String json = decrypt(encrypted);
            if (json != null) {
                BankSettings loaded = GSON.fromJson(json, BankSettings.class);
                if (loaded != null) {
                    currentSettings = loaded;
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading bank_config.dat: " + e.getMessage());
            currentSettings = new BankSettings();
        }
    }

    public static synchronized void save() {
        File file = ConfigManager.getStorageFile("bank_config.dat");
        try {
            String json = GSON.toJson(currentSettings);
            byte[] encrypted = encrypt(json);
            Files.write(file.toPath(), encrypted);
        } catch (Exception e) {
            System.err.println("Error saving bank_config.dat: " + e.getMessage());
        }
    }

    private static byte[] encrypt(String plaintext) throws Exception {
        SecretKey key = deriveKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] result = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);
        return result;
    }

    private static String decrypt(byte[] data) throws Exception {
        if (data.length < GCM_IV_LENGTH + 1) return null;

        SecretKey key = deriveKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(data, 0, iv, 0, GCM_IV_LENGTH);

        byte[] ciphertext = new byte[data.length - GCM_IV_LENGTH];
        System.arraycopy(data, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, StandardCharsets.UTF_8);
    }

    private static SecretKey deriveKey() throws Exception {
        String machineId = getMachineFingerprint();
        KeySpec spec = new PBEKeySpec(machineId.toCharArray(), APP_SALT.getBytes(), 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private static String getMachineFingerprint() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("os.name", ""));
        sb.append(System.getProperty("user.name", ""));
        sb.append(System.getProperty("user.home", ""));

        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                NetworkInterface ni = nets.nextElement();
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    sb.append(Base64.getEncoder().encodeToString(mac));
                }
            }
        } catch (Exception ignored) {}

        return sb.toString();
    }
}
