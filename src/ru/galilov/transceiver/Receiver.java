package ru.galilov.transceiver;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;

public abstract class Receiver {
    private final Deque<Byte> symbolData = new LinkedList<>();
    private final Deque<Byte> removedSymbolData = new LinkedList<>();
    protected Consumer<Integer> dataConsumer;

    public void setDataConsumer(Consumer<Integer> dataConsumer) {
        this.dataConsumer = dataConsumer;
    }

    public void receive(byte[] source) {
        int j = 0;
        while (j < source.length) {
            if (symbolData.size() < getParams().getSamplesPerSymbol()) {
                symbolData.addLast(source[j++]);
            }
            if (symbolData.size() >= getParams().getSamplesPerSymbol()) {
                processSymbolData();
            }
        }
    }

    protected boolean removeFirstSample() {
        if (symbolData.isEmpty()) return false;
        removedSymbolData.addFirst(symbolData.removeFirst());
        removeOldBackedSamples();
        return true;
    }

    private void removeOldBackedSamples() {
        while (removedSymbolData.size() > getParams().getSamplesPerSymbol() * 2) {
            removedSymbolData.removeLast();
        }
    }

    protected void removeAllSamples() {
        while (!symbolData.isEmpty()) {
            removedSymbolData.addFirst(symbolData.removeFirst());
        }
        removeOldBackedSamples();
    }

    protected void removeSamples(int nCount) {
        while (nCount-- > 0 && !symbolData.isEmpty()) {
            removedSymbolData.addFirst(symbolData.removeFirst());
        }
        removeOldBackedSamples();
    }

    protected void restoreFirstSample() {
        if (removedSymbolData.isEmpty()) return;
        symbolData.addFirst(removedSymbolData.removeFirst());
    }

    protected byte[] getSymbolBytes() {
        byte[] data = new byte[getParams().getSamplesPerSymbol()];
        int i = 0;
        for (byte b : symbolData) {
            data[i++] = b;
            if (i == data.length) break;
        }
        return data;
    }


    protected abstract void processSymbolData();

    abstract public SignalParams getParams();
}
