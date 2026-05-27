package com.leaderboard.service;

import java.io.IOException;
import java.util.List;

/**
 * Service Facade for TTSService that routes static calls directly
 * to the active ITTSService instance registered in ServiceLocator.
 * Maintains backwards compatibility across the entire codebase.
 */
public class TTSService {
    private static ITTSService getService() {
        return ServiceLocator.get(ITTSService.class);
    }

    /**
     * Retrieves all available audio output devices in the system.
     */
    public static List<String> getAudioOutputDevices() {
        return getService().getAudioOutputDevices();
    }

    /**
     * Checks if a comment is allowed based on the user-configured blocked words list.
     */
    public static boolean isCommentAllowed(String comment) {
        return getService().isCommentAllowed(comment);
    }

    /**
     * Enqueues a text string to be read aloud sequentially.
     */
    public static void enqueueTTS(String text) {
        getService().enqueueTTS(text);
    }

    /**
     * Enqueues an alert TTS string to be read aloud (ignores global comment TTS disabled check).
     * Used for custom Actions & Events alerts.
     */
    public static void enqueueAlertTTS(String text) {
        getService().enqueueAlertTTS(text);
    }

    /**
     * Immediately stops current playback and clears the remaining TTS queue.
     */
    public static void stopAndClear() {
        getService().stopAndClear();
    }

    /**
     * Dynamically updates the volume of the currently playing source line in decibels.
     */
    public static void updateVolume(double volume) {
        getService().updateVolume(volume);
    }

    /**
     * Decodes and plays a local MP3 file asynchronously through the user's selected audio mixer.
     * Used for sound alerts in Actions & Events.
     */
    public static void playLocalMP3(String filePath, double volume) {
        getService().playLocalMP3(filePath, volume);
    }

    /**
     * Queries Google Translate TTS API and returns audio byte array.
     */
    public static byte[] generateTTSBytes(String text) throws IOException {
        return getService().generateTTSBytes(text);
    }
}
