package com.leaderboard.service;

import com.leaderboard.util.ConfigManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

public class TTSService {
    private static final BlockingQueue<String> ttsQueue = new LinkedBlockingQueue<>();
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    private static boolean isPlaying = false;
    private static SourceDataLine currentLine = null;
    private static Thread playbackThread = null;

    /**
     * Retrieves all available audio output devices (mixers) in the system.
     */
    public static List<String> getAudioOutputDevices() {
        List<String> devices = new ArrayList<>();
        devices.add("Default"); // System default output

        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixers) {
            try {
                Mixer mixer = AudioSystem.getMixer(info);
                // Check if the mixer supports SourceDataLine (which represents audio playback, NOT Ports!)
                boolean supportsPlayback = false;
                for (Line.Info lineInfo : mixer.getSourceLineInfo()) {
                    if (SourceDataLine.class.isAssignableFrom(lineInfo.getLineClass())) {
                        supportsPlayback = true;
                        break;
                    }
                }

                if (supportsPlayback) {
                    String name = info.getName();
                    if (name != null && !name.trim().isEmpty() && !devices.contains(name)) {
                        devices.add(name);
                    }
                }
            } catch (Exception e) {
                // Ignore errors from specific mixers
            }
        }
        return devices;
    }

    private static Mixer.Info getSelectedMixerInfo() {
        String savedName = ConfigManager.getConfig().getTtsAudioDeviceName();
        if (savedName == null || savedName.equalsIgnoreCase("Default")) {
            return null;
        }

        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixers) {
            if (info.getName().equalsIgnoreCase(savedName)) {
                return info;
            }
        }
        return null;
    }

    /**
     * Checks if a comment is allowed based on the user-configured blocked words list.
     */
    public static boolean isCommentAllowed(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            return false;
        }

        String blockedWordsStr = ConfigManager.getConfig().getTtsBlockedWords();
        if (blockedWordsStr == null || blockedWordsStr.trim().isEmpty()) {
            return true;
        }

        String[] words = blockedWordsStr.split("[,\\n]+");
        String lowerComment = comment.toLowerCase();

        for (String word : words) {
            String trimmedWord = word.trim().toLowerCase();
            if (!trimmedWord.isEmpty() && lowerComment.contains(trimmedWord)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Enqueues a text string to be read aloud sequentially.
     */
    public static void enqueueTTS(String text) {
        if (!ConfigManager.getConfig().isTtsEnabled()) {
            return;
        }
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        String cleanText = text.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
        if (cleanText.isEmpty()) {
            return;
        }

        synchronized (ttsQueue) {
            int limit = ConfigManager.getConfig().getTtsMaxQueue();
            while (ttsQueue.size() >= limit) {
                ttsQueue.poll();
            }
            ttsQueue.add(cleanText);
        }

        triggerQueueProcessor();
    }

    /**
     * Immediately stops current playback and clears the remaining TTS queue.
     */
    public static synchronized void stopAndClear() {
        synchronized (ttsQueue) {
            ttsQueue.clear();
        }
        isPlaying = false;

        if (currentLine != null) {
            try {
                currentLine.stop();
                currentLine.flush();
                currentLine.close();
            } catch (Exception e) {
                // Ignore
            }
            currentLine = null;
        }

        if (playbackThread != null && playbackThread.isAlive()) {
            try {
                playbackThread.interrupt();
            } catch (Exception e) {
                // Ignore
            }
            playbackThread = null;
        }
    }

    /**
     * Dynamically updates the volume of the currently playing source line in decibels.
     */
    public static synchronized void updateVolume(double volume) {
        if (currentLine != null && currentLine.isOpen()) {
            try {
                if (currentLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) currentLine.getControl(FloatControl.Type.MASTER_GAIN);
                    float dB = (float) (Math.log(volume == 0.0 ? 0.0001 : volume) / Math.log(10.0) * 20.0);
                    gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
                }
            } catch (Exception e) {
                System.err.println("Failed to adjust MASTER_GAIN: " + e.getMessage());
            }
        }
    }

    private static synchronized void triggerQueueProcessor() {
        if (isPlaying) {
            return;
        }

        String nextText;
        synchronized (ttsQueue) {
            nextText = ttsQueue.poll();
        }

        if (nextText == null) {
            return;
        }

        isPlaying = true;

        playbackThread = new Thread(() -> {
            try {
                byte[] audioData = generateTTSBytes(nextText);
                if (audioData == null || audioData.length == 0) {
                    finishPlaying();
                    return;
                }

                playPCM(audioData);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                finishPlaying();
            }
        });
        playbackThread.start();
    }

    private static void playPCM(byte[] mp3Data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(mp3Data);
        Bitstream bitstream = new Bitstream(bais);
        Decoder decoder = new Decoder();

        try {
            Header header;
            synchronized (TTSService.class) {
                if (!isPlaying) return;
            }

            while ((header = bitstream.readFrame()) != null) {
                SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);
                short[] pcm = output.getBuffer();
                int len = output.getBufferLength();

                synchronized (TTSService.class) {
                    if (currentLine == null) {
                        // TĂNG TỐC ĐỘ ĐỌC LINH HOẠT bằng cách đẩy nhanh Sample Rate của Line
                        int backlog;
                        synchronized (ttsQueue) {
                            backlog = ttsQueue.size();
                        }

                        double speedRate = 1.0;
                        if (backlog > 4) {
                            speedRate = 1.5;
                        } else if (backlog > 2) {
                            speedRate = 1.25;
                        }

                        float baseSampleRate = output.getSampleFrequency();
                        float targetSampleRate = (float) (baseSampleRate * speedRate);

                        AudioFormat format = new AudioFormat(
                                targetSampleRate,
                                16,
                                output.getChannelCount(),
                                true,
                                false
                        );

                        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                        Mixer.Info mixerInfo = getSelectedMixerInfo();

                        if (mixerInfo != null) {
                            try {
                                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                                currentLine = (SourceDataLine) mixer.getLine(info);
                            } catch (Exception ex) {
                                System.err.println("Selected mixer failed to get line: " + ex.getMessage() + ". Falling back to default system device.");
                                currentLine = (SourceDataLine) AudioSystem.getSourceDataLine(format);
                            }
                        } else {
                            currentLine = (SourceDataLine) AudioSystem.getSourceDataLine(format);
                        }

                        currentLine.open(format);
                        currentLine.start();

                        // Set Volume
                        updateVolume(ConfigManager.getConfig().getTtsVolume());
                    }
                }

                byte[] bytes = new byte[len * 2];
                for (int i = 0; i < len; i++) {
                    short val = pcm[i];
                    bytes[i * 2] = (byte) (val & 0xff);
                    bytes[i * 2 + 1] = (byte) ((val >> 8) & 0xff);
                }

                currentLine.write(bytes, 0, bytes.length);
                bitstream.closeFrame();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bitstream.close();
            } catch (Exception e) {
                // Ignore
            }
            
            synchronized (TTSService.class) {
                if (currentLine != null) {
                    try {
                        currentLine.drain();
                        currentLine.close();
                    } catch (Exception e) {
                        // Ignore
                    }
                    currentLine = null;
                }
            }
        }
    }

    private static synchronized void finishPlaying() {
        isPlaying = false;
        playbackThread = null;
        triggerQueueProcessor();
    }

    /**
     * Queries Google Translate TTS API and returns audio byte array.
     */
    public static byte[] generateTTSBytes(String text) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return new byte[0];
        }

        List<String> chunks = chunkText(text, 180);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (String chunk : chunks) {
            String url = "https://translate.googleapis.com/translate_tts?client=gtx&ie=UTF-8&tl=vi&q="
                    + URLEncoder.encode(chunk, StandardCharsets.UTF_8.name());

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    baos.write(response.body().bytes());
                } else {
                    System.err.println("Google TTS API returned error: " + response.code() + " - " + response.message());
                }
            }
        }

        return baos.toByteArray();
    }

    private static List<String> chunkText(String text, int maxLimit) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }

        String[] words = text.split("\\s+");
        StringBuilder currentChunk = new StringBuilder();

        for (String word : words) {
            if (currentChunk.length() + word.length() + 1 > maxLimit) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk.setLength(0);
                }

                if (word.length() > maxLimit) {
                    int index = 0;
                    while (index < word.length()) {
                        int end = Math.min(index + maxLimit, word.length());
                        chunks.add(word.substring(index, end));
                        index = end;
                    }
                } else {
                    currentChunk.append(word).append(" ");
                }
            } else {
                currentChunk.append(word).append(" ");
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
}
