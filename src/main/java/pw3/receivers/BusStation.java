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
            names = {"-mp", "--multicast-port"},
            description = "Port to use for multicast (default: 9876).",
            defaultValue = "9876",
            scope = CommandLine.ScopeType.INHERIT
    )
    protected int multicastPort;

    @CommandLine.Option(
            names = {"-up", "--unicast-port"},
            description = "Port to use for unicast (default: 1234).",
            defaultValue = "1234",
            scope = CommandLine.ScopeType.INHERIT
    )
    protected int unicastPort;

    private void handleMulticast() {
        try (MulticastSocket socket = new MulticastSocket(multicastPort)) {
            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + multicastPort;
            System.out.println("Multicast receiver started (" + myself + ")");

            InetAddress multicastAddress = InetAddress.getByName(hostMulti);
            InetSocketAddress group = new InetSocketAddress(multicastAddress, multicastPort);
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

    private void handleUnicast() {
        // Logique pour gérer la réception Unicast
        try (DatagramSocket socket = new DatagramSocket(unicastPort)) {
            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + unicastPort;
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
        Thread multicastThread = new Thread(this::handleMulticast);
        Thread unicastThread = new Thread(this::handleUnicast);

        multicastThread.start();
        unicastThread.start();

        try {
            multicastThread.join();
            unicastThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }
}



