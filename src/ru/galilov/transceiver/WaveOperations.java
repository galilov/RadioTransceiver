package ru.galilov.transceiver;

public class WaveOperations {
    private final SignalParams signalParams;

    WaveOperations(SignalParams signalParams) {
        this.signalParams = signalParams;
    }

    byte[] generateSineWave(int freq) {
        return generateSineWave(freq, signalParams.getSamplesPerSymbol());
    }

    byte[] generateSineWave(int freq, int nSamples) {
        double kWave = 2 * Math.PI * freq / signalParams.getSampleRate();
        byte[] result = new byte[nSamples];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (Byte.MAX_VALUE * Math.sin(i * kWave));
        }
        return result;
    }

    public byte[] generateSineWave2(int freq1, int freq2) {
        return generateSineWave2(freq1, freq2, signalParams.getSamplesPerSymbol());
    }

    private byte[] generateSineWave2(int freq1, int freq2, int nSamples) {
        double kWave1 = 2 * Math.PI * freq1 / signalParams.getSampleRate();
        double kWave2 = 2 * Math.PI * freq2 / signalParams.getSampleRate();
        byte[] result = new byte[nSamples];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (Byte.MAX_VALUE * (Math.sin(i * kWave1) + Math.sin(i * kWave2)) / 2.0);
        }
        return result;
    }

    public byte[] applyCosWindow(byte[] source) {
        final int n = source.length;
        byte[] result = new byte[n];
        for (int i = 0; i < n; i++) {
            double x = (Math.PI * i / n) - Math.PI / 2;
            double w = Math.cos(x);
            result[i] = (byte) (source[i] * w);
        }
        return result;
    }

    public double[] normalize(byte[] data) {
        int max = 0;
        for (byte b : data) {
            int absB = Math.abs(b);
            if (absB > max) {
                max = absB;
            }
        }
        double[] result = new double[data.length];
        if (max > 0) {
            for (int i = 0; i < result.length; i++) {
                result[i] = data[i] / (double) max;
            }
        } else {
            for (int i = 0; i < result.length; i++) {
                result[i] = 0.0;
            }
        }
        return result;
    }
}
