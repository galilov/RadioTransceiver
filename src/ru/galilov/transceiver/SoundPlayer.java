package ru.galilov.transceiver;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class SoundPlayer {

    private static Logger logger = Logger.getLogger(SoundPlayer.class.getSimpleName());
    private Clip clip;
    private int sampleRate;

    SoundPlayer() {
    }

    void play(byte[] data) {
        assert sampleRate != 0;
        try {
            if (clip != null) {
                clip.stop();
                clip.close();
            } else {
                clip = AudioSystem.getClip();
            }

            Object playSync = new Object();

            LineListener listener = event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.stop();
                    clip.close();
                    clip = null;
                    logger.log(Level.INFO, "Data has been transmitted.");
                    synchronized (playSync) {
                        playSync.notifyAll();
                    }
                }
            };
            clip.addLineListener(listener);

            AudioFormat af = new AudioFormat(
                    sampleRate,
                    8,
                    1,
                    true,
                    false
            );

            AudioInputStream ais = new AudioInputStream(
                    new ByteArrayInputStream(data),
                    af,
                    data.length);

            clip.open(ais);
            logger.log(Level.INFO, "Start data transmitting...");
            clip.start();
            synchronized (playSync) {
                playSync.wait();
            }
        } catch (InterruptedException | IOException | LineUnavailableException exception) {
            throw new RuntimeException(exception);
        }
    }

    void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }
}
