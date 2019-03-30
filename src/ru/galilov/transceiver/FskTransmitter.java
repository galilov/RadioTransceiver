package ru.galilov.transceiver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class FskTransmitter extends Transmitter<FskParams> {

    private final static int LONG_SYNC_LENGTH_MICROSECONDS = 500 * 1000;
    private final WaveOperations wo;
    private final Map<FskParams.CharIndex, byte[]> frequencies = new HashMap<>();

    private final FskParams.CharIndex[] bitToCharIndex = new FskParams.CharIndex[]{
            FskParams.CharIndex.Zero,
            FskParams.CharIndex.One
    };
    private final byte[] longPilotTone;

    public FskTransmitter(FskParams signalParams) {
        super(signalParams);
        wo = new WaveOperations(signalParams);
        for (FskParams.CharIndex charIndex : FskParams.CharIndex.values()) {
            frequencies.put(charIndex, wo.generateSineWave(signalParams.getFrequency(charIndex)));
        }
        int nSamples = LONG_SYNC_LENGTH_MICROSECONDS * signalParams.getSamplesPerSymbol() / signalParams.getSymbolDurationMicroseconds();
        longPilotTone = wo.generateSineWave(signalParams.getFrequency(FskParams.CharIndex.Pilot), nSamples);
    }

    @Override
    public void transmit(byte[] data, SoundPlayer soundPlayer) {
        soundPlayer.setSampleRate(signalParams.getSampleRate());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            addLongPilotTone(baos);
            addDataTones(baos, data);
            addLongPilotTone(baos);
            baos.flush();
            soundPlayer.play(baos.toByteArray());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void addLongPilotTone(OutputStream baos) throws IOException {
        baos.write(longPilotTone);
    }

    private void addDataTones(OutputStream baos, byte[] data) throws IOException {
        for (int b : data) {
            // todo: add redundancy and correction bits for each block
            int previousBit = -1;
            for (int i = 0; i < Byte.SIZE; i++) {
                int currentBit = ((b >>> i) & 0x01);
                if (currentBit == previousBit) {
                    previousBit = -1;
                    addRepeatTone(baos);
                } else {
                    addSymbolTone(baos, bitToCharIndex[currentBit]);
                    previousBit = currentBit;
                }
            }
            // add sync after each block
            addSyncTone(baos);
        }
    }

    private void addSymbolTone(OutputStream baos, FskParams.CharIndex charIndex) throws IOException {
        // System.out.println(charIndex);
        baos.write(frequencies.get(charIndex));
    }

    private void addRepeatTone(OutputStream baos) throws IOException {
        addSymbolTone(baos, FskParams.CharIndex.Repeat);
    }

    private void addSyncTone(OutputStream baos) throws IOException {
        addSymbolTone(baos, FskParams.CharIndex.Sync);
    }

}
