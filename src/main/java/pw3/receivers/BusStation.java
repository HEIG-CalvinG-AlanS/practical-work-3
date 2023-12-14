package pw3.receivers;


import picocli.CommandLine;
import pw3.emitters.AbstractEmitter;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@CommandLine.Command(
        name = "bus-station",
        description = "Start an UDP multicast receiver"
)
public class BusStation extends AbstractEmitter {

    protected String hostMulti = "239.0.0.1";

    @CommandLine.Option(
            names = {"-i", "--interface"},
            description = "Interface to use",
            scope = CommandLine.ScopeType.INHERIT,
            required = true
    )
    private String interfaceName;

    @CommandLine.Option(
            names = {"-p", "--port"},
            description = "port to use.",
            scope = CommandLine.ScopeType.INHERIT,
            required = true
    )
    private int portMulti;

    private void handleMulticast() {
        try (MulticastSocket socket = new MulticastSocket(portMulti)) {
            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + portMulti;
            System.out.println("Multicast receiver started (" + myself + ")");

            InetAddress multicastAddress = InetAddress.getByName(hostMulti);
            InetSocketAddress group = new InetSocketAddress(multicastAddress, portMulti);
            NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
            socket.joinGroup(group, networkInterface);

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
                System.out.println("Multicast receiver (" + myself + ") received message: " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int PORT_UNI = 1234;
    private static final String HOST_UNI = "localhost";

    private void handleReceiverUnicast() {
        // Logique pour gérer la réception Unicast
        try (DatagramSocket socket = new DatagramSocket(PORT_UNI)) {
            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + PORT_UNI;
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



    private void handleEmitterUnicast() {
        try (DatagramSocket socket = new DatagramSocket()) {
            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + PORT_UNI;
            System.out.println("Unicast emitter started (" + myself + ")");

            InetAddress serverAddress = InetAddress.getByName(HOST_UNI);
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

            scheduler.scheduleAtFixedRate(() -> {
                try {
                    String message = "Hello, from bus station emitter! (" + myself + ")";

                    byte[] payload = message.getBytes(StandardCharsets.UTF_8);

                    DatagramPacket datagram = new DatagramPacket(
                            payload,
                            payload.length,
                            serverAddress,
                            PORT_UNI
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

    @Override
    public Integer call() {
        Thread multicastThread = new Thread(this::handleMulticast);
        Thread unicastReceiverThread = new Thread(this::handleReceiverUnicast);
        Thread unicastEmitterThread = new Thread(this::handleEmitterUnicast);

        multicastThread.start();
        unicastReceiverThread.start();
        unicastEmitterThread.start();

        try {
            multicastThread.join();
            unicastReceiverThread.join();
            unicastEmitterThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }
}



