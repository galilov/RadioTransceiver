package ru.galilov.transceiver;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class FskReceiver extends Receiver implements Consumer<Goertzel> {
    private final FskParams fskParams;
    private Goertzel[] goertzels = new Goertzel[FskParams.CharIndex.values().length];

    FskReceiver(FskParams fskParams) {
        this.fskParams = fskParams;
        FskParams.CharIndex[] values = FskParams.CharIndex.values();
        for (int i = 0; i < values.length; i++) {
            goertzels[i] = new Goertzel(fskParams, fskParams.getFrequency(values[i]), 1, this);
            goertzels[i].setTag(values[i]);
        }
    }

    private final Set<FskParams.CharIndex> results = new HashSet<>();
    private int currentBlock = 0;
    private FskParams.CharIndex previousCharacter = FskParams.CharIndex.Sync;
    private int indexInBlock = 0;

    @Override
    public void accept(Goertzel goertzel) {
        FskParams.CharIndex charIndex = (FskParams.CharIndex) goertzel.getTag();
        results.add(charIndex);
        if (results.size() != goertzels.length) return;
        results.clear();
        Goertzel max = getMaxPowerGoertzel();
        if (max == null) return;
        FskParams.CharIndex currentChar = (FskParams.CharIndex) max.getTag();
        FskParams.CharIndex[] values = FskParams.CharIndex.values();
        if (currentChar == FskParams.CharIndex.Pilot) {
            removeSamples((fskParams.getSamplesPerSymbol() / 4 - 1));
        } else {
            removeSamples((fskParams.getSamplesPerSymbol() / 2 - 1));
        }
        switch (currentChar) {
            case Zero:
                processZero();
                break;
            case One:
                processOne();
                break;
            case Repeat:
                processRepeat();
                break;
            case Sync:
                if (previousCharacter != FskParams.CharIndex.Sync) {
                    if (dataConsumer != null) {
                        if (indexInBlock != Byte.SIZE) {
                            dataConsumer.accept(-1);
                        } else {
                            dataConsumer.accept(currentBlock);
                        }
                    }
                    indexInBlock = 0;
                    currentBlock = 0;
                }
                break;
            case Pilot:
                if (previousCharacter != FskParams.CharIndex.Pilot) {
                    if (dataConsumer != null) {
                        dataConsumer.accept(-2);
                    }
                    indexInBlock = 0;
                    currentBlock = 0;
                }
                break;
        }
        previousCharacter = currentChar;
        //counts[currentChar.value] = 0;
    }

    private void processRepeat() {
        if (previousCharacter != FskParams.CharIndex.Repeat) {
            //System.out.print("R");
            switch (previousCharacter) {
                case Zero:
                    appendZero();
                    break;
                case One:
                    appendOne();
                    break;
            }
        }
    }

    private void processOne() {
        if (previousCharacter != FskParams.CharIndex.One) {
            appendOne();
        }
    }

    private void appendOne() {
        // System.out.print("1");
        currentBlock |= (1 << (indexInBlock++));
    }

    private void processZero() {
        if (previousCharacter != FskParams.CharIndex.Zero) {
            appendZero();
        }
    }

    private void appendZero() {
        //   System.out.print("0");
        currentBlock &= (~(1 << (indexInBlock++)));
    }

    private Goertzel getMaxPowerGoertzel() {
        double maxMagnitude = 0;
        double maxMagnitude1 = 0;
        Goertzel result = null;

        for (Goertzel g : goertzels) {
            if (maxMagnitude < g.getMagnitude()) {
                maxMagnitude = g.getMagnitude();
                result = g;
            }
        }
        for (Goertzel g : goertzels) {
            if (maxMagnitude1 < g.getMagnitude() && g != result) {
                maxMagnitude1 = g.getMagnitude();
            }
        }
        if (maxMagnitude < 80
                || (maxMagnitude1 > 0 && maxMagnitude / maxMagnitude1 < 2)
        ) return null;
        //if (result.getTag() != FskParams.CharIndex.Sync) {
        //System.out.println("[" + result.getTag() + ": " + maxMagnitudeSquared / maxMagnitudeSquared1 + "]");
        //}
        return result;
    }

    private void processSample(double sample) {
        for (Goertzel g : goertzels) {
            g.process(sample);
        }
    }

    @Override
    protected void processSymbolData() {
        for (byte sample : getSymbolBytes()) {
            processSample(sample);
        }
        removeSamples(1);
        //
        //removeFirstSample();
        //removeSamples(1);
    }

    @Override
    public SignalParams getParams() {
        return fskParams;
    }
}
