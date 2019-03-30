package ru.galilov.transceiver;

public interface SignalParams {
    int getSampleRate();

    int getSymbolDurationMicroseconds();

    int getBlockPrefixLenSymbols();

    int getSamplesPerSymbol();
}
