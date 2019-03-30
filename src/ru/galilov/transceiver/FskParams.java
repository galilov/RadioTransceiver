package ru.galilov.transceiver;

public class FskParams implements SignalParams {

    public enum CharIndex {
        Zero(0),
        One(1),
        Repeat(2),
        Sync(3),
        Pilot(4);
        public final int value;

        CharIndex(int value) {
            this.value = value;
        }
    }

    private static int MICROSECONDS = 1000000;

    private int symbolDataLength = -1;

    private final int[] frequencies = new int[]{2570, 2210, 1860, 1470, 830};

    public FskParams() {

    }

    public int getFrequency(CharIndex charIndex) {
        return frequencies[charIndex.value];
    }


    @Override
    public int getSampleRate() {
        return 8000;
    }

    @Override
    public int getSymbolDurationMicroseconds() {
        return 5000;
    }

    @Override
    public int getBlockPrefixLenSymbols() {
        return 0;
    }

    @Override
    public int getSamplesPerSymbol() {
        if (symbolDataLength == -1) {
            symbolDataLength = getSymbolDurationMicroseconds() * getSampleRate() / MICROSECONDS;
        }
        return symbolDataLength;
    }

}
