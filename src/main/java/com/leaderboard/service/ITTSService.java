package com.leaderboard.service;

import java.io.IOException;
import java.util.List;

/**
 * Interface representing Text-to-Speech and sound alerts playback services.
 */
public interface ITTSService {
    /**
     * Retrieves all available audio output devices in the system.
     */
    List<String> getAudioOutputDevices();

    /**
     * Checks if a comment is allowed based on blocked words configuration.
     */
    boolean isCommentAllowed(String comment);

    /**
     * Enqueues a comment or text to be read aloud sequentially.
     */
    void enqueueTTS(String text);

    /**
     * Enqueues a priority alert text to be read aloud.
     */
    void enqueueAlertTTS(String text);

    /**
     * Immediately stops playback and clears queues.
     */
    void stopAndClear();

    /**
     * Updates the gain volume of the active playing line.
     */
    void updateVolume(double volume);

    /**
     * Plays a local MP3 file asynchronously using the configured mixer.
     */
    void playLocalMP3(String filePath, double volume);

    /**
     * Contacts Google Translate TTS API and returns the audio chunk byte array.
     */
    byte[] generateTTSBytes(String text) throws IOException;
}
