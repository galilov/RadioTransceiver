package ru.galilov.transceiver;

import javax.sound.sampled.*;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

class SoundRecorder {

    private static Logger logger = Logger.getLogger(SoundRecorder.class.getSimpleName());
    private Receiver receiver;
    private Thread thrReadAudio;
    private Thread thrReceiver;
    private TargetDataLine targetDataLine;
    private final ConcurrentLinkedQueue<byte[]> audioQueue = new ConcurrentLinkedQueue<>();

    SoundRecorder(Receiver receiver) {
        this.receiver = receiver;
    }

    void start() {
        try {
            AudioFormat format = new AudioFormat(receiver.getParams().getSampleRate(),
                    8, 1, true, false);

            // microphone = AudioSystem.getTargetDataLine(format);

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(format);
            thrReadAudio = new Thread(() -> {
                targetDataLine.start();
                byte[] data = new byte[targetDataLine.getBufferSize() / 3];
                while (!thrReadAudio.isInterrupted()) {
                    int nBytesRead = targetDataLine.read(data, 0, data.length);
                    if (nBytesRead > 0) {
                        byte[] readData = Arrays.copyOf(data, nBytesRead);
                        synchronized (audioQueue) {
                            audioQueue.add(readData);
                            audioQueue.notify();
                        }
                    }
                    if (nBytesRead < data.length) {
                        break;
                    }
                }
            });
            thrReceiver = new Thread(() -> {
                try {
                    while (!thrReceiver.isInterrupted()) {
                        byte[] readData;
                        synchronized (audioQueue) {
                            do {
                                audioQueue.wait();
                                readData = audioQueue.poll();
                            } while (readData == null);
                        }
                        receiver.receive(readData);
                    }
                } catch (InterruptedException exception) {
                    logger.log(Level.WARNING, "Interrupted");
                }
            });
            thrReadAudio.start();
            thrReceiver.start();
        } catch (LineUnavailableException exception) {
            throw new RuntimeException(exception);
        }
    }

    void stop() {
        try {
            if (targetDataLine != null) {
                thrReadAudio.interrupt();
                thrReceiver.interrupt();
                thrReadAudio.join();
                thrReceiver.join();
                targetDataLine.close();
                targetDataLine = null;
            }
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }
}
