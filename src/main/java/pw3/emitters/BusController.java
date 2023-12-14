package pw3.emitters;

import picocli.CommandLine.Command;
import pw3.emitters.AbstractEmitter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Command(
        name = "bus-controller-emitter",
        description = "Start an UDP unicast emitter"
)
public class BusController extends AbstractEmitter {

    private static final int PORT = 1234;
    private static final String HOST = "localhost";

    private void handleEmitterUnicast() {
        try (DatagramSocket socket = new DatagramSocket()) {
            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + PORT;
            System.out.println("Unicast emitter started (" + myself + ")");

            InetAddress serverAddress = InetAddress.getByName(HOST);
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

            scheduler.scheduleAtFixedRate(() -> {
                try {
                    String message = "Hello, from unicast bus controller emitter! (" + myself + ")";

                    byte[] payload = message.getBytes(StandardCharsets.UTF_8);

                    DatagramPacket datagram = new DatagramPacket(
                            payload,
                            payload.length,
                            serverAddress,
                            PORT
                    );

                    socket.send(datagram);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, delay, frequency, TimeUnit.MILLISECONDS);

            // Keep the program running for a while
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleReceiverUnicast() {
// Logique pour gérer la réception Unicast
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + PORT;
            System.out.println("Unicast receiver started (" + myself + ")");

            byte[] receiveData = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(
                        receiveData,
                        receiveData.length
                );

                socket.receive(packet);

                String message = new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                System.out.println("Unicast receiver (" + myself + ") received message: " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer call() {
        Thread unicastReceiverThread = new Thread(this::handleReceiverUnicast);
        Thread unicastEmitterThread = new Thread(this::handleEmitterUnicast);

        unicastReceiverThread.start();
        unicastEmitterThread.start();

        try {
            unicastReceiverThread.join();
            unicastEmitterThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }
}
