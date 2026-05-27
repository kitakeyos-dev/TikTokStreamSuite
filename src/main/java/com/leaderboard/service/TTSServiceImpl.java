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

/**
 * Implementation of ITTSService encapsulating instance variables and multithreading.
 */
public class TTSServiceImpl implements ITTSService {
    private final BlockingQueue<String> ttsQueue = new LinkedBlockingQueue<>();
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    private boolean isPlaying = false;
    private SourceDataLine currentLine = null;
    private Thread playbackThread = null;

    @Override
    public List<String> getAudioOutputDevices() {
        List<String> devices = new ArrayList<>();
        devices.add("Default"); // System default output

        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixers) {
            try {
                Mixer mixer = AudioSystem.getMixer(info);
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
                // Ignore
            }
        }
        return devices;
    }

    private Mixer.Info getSelectedMixerInfo() {
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

    @Override
    public boolean isCommentAllowed(String comment) {
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

    @Override
    public void enqueueTTS(String text) {
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

    @Override
    public void enqueueAlertTTS(String text) {
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

    @Override
    public synchronized void stopAndClear() {
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

    @Override
    public synchronized void updateVolume(double volume) {
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

    private synchronized void triggerQueueProcessor() {
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

    private void playPCM(byte[] mp3Data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(mp3Data);
        Bitstream bitstream = new Bitstream(bais);
        Decoder decoder = new Decoder();

        try {
            Header header;
            synchronized (this) {
                if (!isPlaying) return;
            }

            while ((header = bitstream.readFrame()) != null) {
                SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);
                short[] pcm = output.getBuffer();
                int len = output.getBufferLength();

                synchronized (this) {
                    if (currentLine == null) {
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
                                System.err.println("Selected mixer failed: " + ex.getMessage() + ". Fallback to default.");
                                currentLine = AudioSystem.getSourceDataLine(format);
                            }
                        } else {
                            currentLine = AudioSystem.getSourceDataLine(format);
                        }

                        currentLine.open(format);
                        currentLine.start();

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
            
            synchronized (this) {
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

    private synchronized void finishPlaying() {
        isPlaying = false;
        playbackThread = null;
        triggerQueueProcessor();
    }

    @Override
    public void playLocalMP3(String filePath, double volume) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return;
        }
        
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.err.println("Sound alert file not found: " + filePath);
            return;
        }

        new Thread(() -> {
            SourceDataLine line = null;
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                 
                Bitstream bitstream = new Bitstream(bis);
                Decoder decoder = new Decoder();
                Header header;
                
                while ((header = bitstream.readFrame()) != null) {
                    SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);
                    short[] pcm = output.getBuffer();
                    int len = output.getBufferLength();
                    
                    if (line == null) {
                        AudioFormat format = new AudioFormat(
                                output.getSampleFrequency(),
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
                                line = (SourceDataLine) mixer.getLine(info);
                            } catch (Exception ex) {
                                line = AudioSystem.getSourceDataLine(format);
                            }
                        } else {
                            line = AudioSystem.getSourceDataLine(format);
                        }
                        
                        line.open(format);
                        line.start();
                        
                        if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                            FloatControl gainControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                            float dB = (float) (Math.log(volume == 0.0 ? 0.0001 : volume) / Math.log(10.0) * 20.0);
                            gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
                        }
                    }
                    
                    byte[] bytes = new byte[len * 2];
                    for (int i = 0; i < len; i++) {
                        short val = pcm[i];
                        bytes[i * 2] = (byte) (val & 0xff);
                        bytes[i * 2 + 1] = (byte) ((val >> 8) & 0xff);
                    }
                    
                    line.write(bytes, 0, bytes.length);
                    bitstream.closeFrame();
                }
                
                if (line != null) {
                    line.drain();
                }
            } catch (Exception e) {
                System.err.println("Error playing local MP3: " + e.getMessage());
            } finally {
                if (line != null) {
                    try {
                        line.close();
                    } catch (Exception ex) {
                        // Ignore
                    }
                }
            }
        }).start();
    }

    @Override
    public byte[] generateTTSBytes(String text) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return new byte[0];
        }

        List<String> chunks = chunkText(text, 180);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (String chunk : chunks) {
            String url = "https://translate.googleapis.com/translate_tts?client=gtx&ie=UTF-8&tl=vi&q="
                    + URLEncoder.encode(chunk, StandardCharsets.UTF_8);

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    baos.write(response.body().bytes());
                }
            }
        }

        return baos.toByteArray();
    }

    private List<String> chunkText(String text, int maxLimit) {
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
