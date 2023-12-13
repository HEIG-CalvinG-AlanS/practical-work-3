package pw3.receivers;


import picocli.CommandLine;

import java.net.*;
import java.nio.charset.StandardCharsets;

@CommandLine.Command(
        name = "bus-station-receiver",
        description = "Start an UDP multicast receiver"
)
public class BusStationReceiver extends AbstractReceiver {

    protected String host = "239.0.0.1";

    @CommandLine.Option(
            names = {"-i", "--interface"},
            description = "Interface to use",
            scope = CommandLine.ScopeType.INHERIT,
            required = true
    )
    private String interfaceName;

    private static final int PORT = 1234;

    @Override
    public Integer call() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + PORT;
            System.out.println("Multicast receiver started (" + myself + ")");

            InetAddress multicastAddress = InetAddress.getByName(host);
            InetSocketAddress group = new InetSocketAddress(multicastAddress, PORT);
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
            return 1;
        }
    }
}

