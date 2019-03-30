package ru.galilov.transceiver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        Thread thrReceiver = new Thread(() -> doReceive());

        thrReceiver.setDaemon(true);
        thrReceiver.start();
        doTransmit();
    }

    private static void doTransmit() {
        FskParams params = new FskParams();
        String line;
        do {
            System.out.print(">");
            line = System.console().readLine();
            if ("/exit".equals(line)) break;
            byte[] data = line.getBytes(StandardCharsets.UTF_8);
            FskTransmitter transmitter = new FskTransmitter(params);
            SoundPlayer soundPlayer = new SoundPlayer();
            transmitter.transmit(data, soundPlayer);
        } while (true);
        System.exit(0);
    }

    private static void doReceive() {
        FskParams params = new FskParams();
        final List<Integer> readBytes = new LinkedList<>();
        FskReceiver receiver = new FskReceiver(params);
        final SoundRecorder soundRecorder = new SoundRecorder(receiver);
        receiver.setDataConsumer(data -> {
            if (data == -2 && readBytes.size() > 0) {
                byte[] arr = new byte[readBytes.size()];
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = readBytes.get(i).byteValue();
                }
                readBytes.clear();
                System.out.println(new String(arr, StandardCharsets.UTF_8));
            } else if (data == -1) {
                readBytes.add((int) '#');
            } else {
                readBytes.add(data);
            }

        });
        soundRecorder.start();
    }


}
