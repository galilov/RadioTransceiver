package ru.galilov.transceiver;

import java.util.function.Consumer;

class Goertzel {
    //https://www.embedded.com/design/configurable-systems/4024443/The-Goertzel-Algorithm

    private final SignalParams signalParams;
    private final double targetFreq;
    private Consumer<Goertzel> resultConsumer;
    private double cosine, sine;
    private double coeff;
    private double q1, q2;
    private int nIterationCount;
    private double magnitudeSquared;
    private double magnitude;
    private int n;
    private Object tag;
    private double scale;

    Goertzel(SignalParams signalParams, double targetFreq, double scale, Consumer<Goertzel> resultConsumer) {
        this.signalParams = signalParams;
        this.targetFreq = targetFreq;
        this.resultConsumer = resultConsumer;
        this.scale = scale;
        reset();
    }

    void setTag(Object tag) {
        this.tag = tag;
    }

    Object getTag() {
        return tag;
    }

    public double getTargetFreq() {
        return targetFreq;
    }

    public double getMagnitudeSquared() {
        return magnitudeSquared;
    }

    double getMagnitude() {
        return magnitude;
    }

    private void reset() {
        n = (int) (signalParams.getSamplesPerSymbol() * scale);
        int sampleRate = signalParams.getSampleRate();
        int k = (int) (0.5 + n * targetFreq / sampleRate);
        double w = 2 * Math.PI * k / n;
        cosine = Math.cos(w);
        sine = Math.sin(w);
        coeff = 2 * cosine;
        q1 = 0;
        q2 = 0;
        nIterationCount = 0;
    }

    boolean process(double sample) {
        double q0 = coeff * q1 - q2 + sample;
        q2 = q1;
        q1 = q0;
        boolean done = ++nIterationCount >= n;
        if (done) {
            //double real = q1 - q2 * cosine;
            //double imag = q2 * sine;
            //magnitudeSquared = real * real + imag * imag;
            magnitudeSquared = q1 * q1 + q2 * q2 - q1 * q2 * coeff;
            magnitude = Math.sqrt(magnitudeSquared);
            resultConsumer.accept(this);
            reset();
            //real = (q1 - q2 * cosine)
            //imag = (q2 * sine)
        }
        return done;
    }
}
