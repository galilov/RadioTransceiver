package ru.galilov.transceiver;

public abstract class Transmitter<SIGNAL_PARAMS extends SignalParams> {

    final SIGNAL_PARAMS signalParams;

    Transmitter(SIGNAL_PARAMS signalParams) {
        this.signalParams = signalParams;
    }

    public abstract void transmit(byte[] data, SoundPlayer soundPlayer);


}
