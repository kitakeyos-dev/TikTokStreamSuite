package com.leaderboard.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.leaderboard.model.BankDeposit;
import com.leaderboard.model.BankTransaction;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/**
 * Local bank deposit history and API push, stored alongside other app data.
 */
public class BankDataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<BankDeposit>>() {}.getType();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    private static final List<BankDeposit> records = new ArrayList<>();
    private static final Set<String> knownKeys = new LinkedHashSet<>();

    private static String apiUrl = "";
    private static String apiKey = "";
    private static int lastMatchedCount = 0;
    private static String lastError = null;
    private static Consumer<String> logCallback;

    static {
        load();
    }

    public static File getDataFile() {
        return ConfigManager.getStorageFile("bank_deposits.json");
    }

    public static synchronized void setApiConfig(String url, String key) {
        apiUrl = url != null ? url.replaceAll("/+$", "") : "";
        apiKey = key != null ? key : "";
    }

    public static synchronized void setLogCallback(Consumer<String> callback) {
        logCallback = callback;
    }

    public static synchronized String getLastError() {
        return lastError;
    }

    public static synchronized int getLastMatchedCount() {
        return lastMatchedCount;
    }

    public static synchronized List<BankDeposit> getDeposits() {
        return List.copyOf(records);
    }

    public static synchronized void load() {
        File file = getDataFile();
        records.clear();
        knownKeys.clear();

        if (!file.exists()) {
            return;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            List<BankDeposit> loaded = GSON.fromJson(reader, LIST_TYPE);
            if (loaded != null) {
                records.addAll(loaded);
                for (BankDeposit deposit : records) {
                    knownKeys.add(deposit.uniqueKey());
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading bank_deposits.json: " + e.getMessage());
        }
    }

    public static synchronized void save() {
        File file = getDataFile();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(records, writer);
        } catch (Exception e) {
            System.err.println("Error saving bank_deposits.json: " + e.getMessage());
        }
    }

    /**
     * Push incoming transactions to the donate API and persist new local records.
     *
     * @return number of new local records added
     */
    public static synchronized int pushTransactions(List<BankTransaction> transactions) {
        lastMatchedCount = 0;

        List<BankTransaction> incoming = new ArrayList<>();
        for (BankTransaction tx : transactions) {
            if (tx.getType() == BankTransaction.Type.IN) {
                incoming.add(tx);
            }
        }

        if (incoming.isEmpty()) {
            return 0;
        }

        pushBatchToApi(incoming);

        int added = 0;
        String now = LocalDateTime.now().format(FMT);
        for (BankTransaction tx : incoming) {
            BankDeposit deposit = new BankDeposit(
                    "",
                    tx.getAmount(),
                    tx.getMessage(),
                    tx.getTime(),
                    tx.getReference(),
                    now
            );

            if (knownKeys.add(deposit.uniqueKey())) {
                records.add(0, deposit);
                added++;
            }
        }

        if (added > 0) {
            save();
        }
        return added;
    }

    private static void pushBatchToApi(List<BankTransaction> incoming) {
        if (apiUrl.isEmpty() || apiKey.isEmpty()) {
            return;
        }

        try {
            List<Map<String, Object>> txList = new ArrayList<>();
            for (BankTransaction tx : incoming) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("amount", tx.getAmount());
                item.put("message", tx.getMessage());
                item.put("time", tx.getTime());
                item.put("reference", tx.getReference());
                txList.add(item);
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("transactions", txList);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/api/deposits"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            lastError = null;
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = GSON.fromJson(response.body(), Map.class);
                    Object matched = result.get("matched");
                    if (matched instanceof Number) {
                        lastMatchedCount = ((Number) matched).intValue();
                    }
                } catch (Exception ignored) {}
                log("[API] Push OK: " + incoming.size() + " giao dịch, matched: " + lastMatchedCount);
            } else {
                lastError = "HTTP " + response.statusCode();
                String bodyStr = response.body();
                if (bodyStr.contains("Cloudflare") || bodyStr.contains("cf-")
                        || response.statusCode() == 403 || response.statusCode() == 503) {
                    log("[API] BỊ CHẶN bởi Cloudflare (HTTP " + response.statusCode()
                            + "). Cần whitelist IP hoặc bypass WAF cho /api/*");
                } else {
                    log("[API] Push thất bại: HTTP " + response.statusCode() + " - "
                            + bodyStr.substring(0, Math.min(200, bodyStr.length())));
                }
                System.err.println("[API] Push failed: HTTP " + response.statusCode() + " - " + bodyStr);
            }
        } catch (Exception e) {
            lastError = e.getMessage();
            log("[API] Lỗi kết nối: " + e.getMessage());
            System.err.println("[API] Push error: " + e.getMessage());
        }
    }

    private static void log(String msg) {
        if (logCallback != null) {
            logCallback.accept(msg);
        }
    }
}
