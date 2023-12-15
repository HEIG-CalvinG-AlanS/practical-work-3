package pw3.receivers;


import picocli.CommandLine;
import pw3.emitters.AbstractEmitter;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;

@CommandLine.Command(
        name = "bus-station",
        description = "Start an UDP multicast receiver"
)
public class BusStation extends AbstractEmitter {
    @CommandLine.Option(
            names = {"-H", "--host"},
            description = "Subnet range/multicast address to use.",
            scope = CommandLine.ScopeType.INHERIT,
            required = true
    )
    protected String host;

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

    private final ConcurrentHashMap<String, LocalTime> lastTimeBusesWereSeen = new ConcurrentHashMap<>();

    private void handleMulticast() {
        try (MulticastSocket socket = new MulticastSocket(multicastPort)) {
            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + multicastPort;
            System.out.println("Multicast receiver started (" + myself + ")");

            InetAddress multicastAddress = InetAddress.getByName(host);
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

                String[] busInfo = message.split(" ");

                String busName = busInfo[1];

                // Store the bus name and the time it was seen
                lastTimeBusesWereSeen.put(busName, LocalTime.now());

                System.out.println(lastTimeBusesWereSeen);
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

                String responseMessage = Arrays.toString(lastTimeBusesWereSeen.entrySet().toArray());

                DatagramPacket response = new DatagramPacket(
                        responseMessage.getBytes(StandardCharsets.UTF_8),
                        responseMessage.length(),
                        packet.getAddress(),
                        packet.getPort()
                );

                socket.send(response);
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



