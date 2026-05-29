package com.leaderboard.util;

import javafx.application.Platform;
import javafx.scene.image.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class ImageCacheManager {
    // Persistent cache directory
    private static final File CACHE_DIR = new File("cache/images");
    private static final ConcurrentHashMap<String, Boolean> activeDownloads = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Image> memoryCache = new ConcurrentHashMap<>();

    static {
        if (!CACHE_DIR.exists()) {
            CACHE_DIR.mkdirs();
        }
    }

    private ImageCacheManager() {
        // Prevent instantiation
    }

    /**
     * Loads an image from the memory cache, local disk cache, or downloads it asynchronously.
     *
     * @param imageUrl  The URL of the remote image.
     * @param width     Requested width.
     * @param height    Requested height.
     * @param onLoaded  Callback invoked on the JavaFX Application Thread when the image is ready.
     * @param fallback  Placeholder image to use while loading or if loading fails.
     */
    public static void loadImage(String imageUrl, double width, double height, Consumer<Image> onLoaded, Image fallback) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            onLoaded.accept(fallback);
            return;
        }

        String cacheKey = getCacheKey(imageUrl, width, height);

        // 1. Check in-memory cache first
        Image memImg = memoryCache.get(cacheKey);
        if (memImg != null) {
            onLoaded.accept(memImg);
            return;
        }

        String fileName = getCacheFileName(imageUrl);
        File cacheFile = new File(CACHE_DIR, fileName);

        // 2. Check persistent disk cache
        if (cacheFile.exists()) {
            try {
                Image diskImg = new Image(cacheFile.toURI().toString(), width, height, true, true, true);
                if (diskImg.isError()) {
                    // Deleted corrupted file and fallback to download
                    cacheFile.delete();
                    onLoaded.accept(fallback);
                    triggerDownload(imageUrl, width, height, onLoaded, fallback, cacheFile, cacheKey);
                } else {
                    memoryCache.put(cacheKey, diskImg);
                    onLoaded.accept(diskImg);
                }
            } catch (Exception e) {
                cacheFile.delete();
                onLoaded.accept(fallback);
                triggerDownload(imageUrl, width, height, onLoaded, fallback, cacheFile, cacheKey);
            }
        } else {
            // Apply fallback/placeholder immediately
            onLoaded.accept(fallback);
            // Download in background thread
            triggerDownload(imageUrl, width, height, onLoaded, fallback, cacheFile, cacheKey);
        }
    }

    private static void triggerDownload(String imageUrl, double width, double height, Consumer<Image> onLoaded, Image fallback, File cacheFile, String cacheKey) {
        // Prevent duplicate concurrent downloads for the same URL
        if (activeDownloads.putIfAbsent(imageUrl, Boolean.TRUE) == null) {
            new Thread(() -> {
                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        // Use temporary file to avoid half-download corruption
                        File tempFile = File.createTempFile("img_", ".tmp", CACHE_DIR);
                        try (InputStream in = conn.getInputStream();
                             FileOutputStream out = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                        }

                        if (tempFile.renameTo(cacheFile)) {
                            Platform.runLater(() -> {
                                Image finalImg = new Image(cacheFile.toURI().toString(), width, height, true, true, true);
                                if (!finalImg.isError()) {
                                    memoryCache.put(cacheKey, finalImg);
                                    onLoaded.accept(finalImg);
                                } else {
                                    cacheFile.delete();
                                    onLoaded.accept(fallback);
                                }
                            });
                        } else {
                            tempFile.delete();
                        }
                    }
                } catch (Exception e) {
                    if (cacheFile.exists()) {
                        cacheFile.delete();
                    }
                } finally {
                    activeDownloads.remove(imageUrl);
                }
            }).start();
        }
    }

    private static String getCacheKey(String url, double w, double h) {
        return url + "_" + w + "_" + h;
    }

    private static String getCacheFileName(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(url.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString() + ".png";
        } catch (Exception e) {
            return String.valueOf(url.hashCode()) + ".png";
        }
    }
}
