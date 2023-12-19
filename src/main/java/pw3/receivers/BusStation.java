package pw3.receivers;

import picocli.CommandLine;
import pw3.emitters.AbstractEmitter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/**
 * Represents a UDP multicast and unicast receiver for bus station data.
 * This class handles receiving data about bus statuses and updates a concurrent map with the latest information.
 */
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

    // Concurrent map to store the last time buses were seen
    private final ConcurrentHashMap<String, String> lastTimeBusesWereSeen = new ConcurrentHashMap<>();

    /**
     * Adds a new entry in the map of buses.
     *
     * @param s An array of strings containing bus information.
     */
    public void addChangesToMap(String[] s){
        String key = s[0] + " " + s[1] + " " + s[2] + " " + s[3];
        String value = s[4] + " " + s[5];
        lastTimeBusesWereSeen.put(key, value);
    }

    /**
     * Handles the multicast reception of bus data.
     * This method sets up a multicast socket and continuously listens for incoming data, updating the map accordingly.
     */
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

                addChangesToMap(message.split(" "));
            }
        } catch (Exception e) {
            System.out.println("There has been an issue while receive Multicast message. " + e);
        }
    }


    /**
     * Processes a unicast message and generates a response based on the current state of buses.
     *
     * This method parses the received message to determine the command (ALL, LINE, BUS) and
     * optionally the line and bus numbers. It then constructs a response string based on the
     * command and the data stored in the map of last seen times for buses.
     *
     * @param message The received message string to process.
     * @return A response string to be sent back to the requester.
     */
    private String sendResponseUnicast(String message) {
        // Get command and arguments
        String[] msg = message.split(" ");
        String command = msg[0];
        String line = "";
        String bus = "";
        if(msg.length >= 2) line = msg[1];
        if(msg.length >= 3) bus = msg[2];

        // Add response to the requester
        String responseMessage = "OK ";

        // Check if the map is empty to return an error
        boolean isEmpty = true;

        // Determine the response based on the commmand
        switch (command) {
            case "ALL":
                for (String name: lastTimeBusesWereSeen.keySet())
                    responseMessage += name + " " + lastTimeBusesWereSeen.get(name) + ',';
                if(!responseMessage.equals("OK "))
                    responseMessage = responseMessage.substring(0, responseMessage.length() - 1);
                break;
            case "LINE":
                for (String name : lastTimeBusesWereSeen.keySet())
                    if(name.split(" ")[1].equals(line)) {
                        responseMessage += name + " " + lastTimeBusesWereSeen.get(name) + ',';
                        isEmpty = false;
                    }
                break;
            case "BUS":
                for (String name: lastTimeBusesWereSeen.keySet())
                    if(name.equals("LINE " + line + " BUS " + bus)) {
                        responseMessage += name + " " + lastTimeBusesWereSeen.get(name) + ',';
                        isEmpty = false;
                    }
                break;
        }

        // If map empty, we return an error
        if(isEmpty)
        {
            if(command.equals("LINE")) responseMessage = "ERROR LINE";
            else if (command.equals("BUS")) responseMessage = "ERROR BUS";
        }

        return responseMessage;
    }

    /**
     * Handles the unicast reception of bus data.
     * This method sets up a unicast socket and continuously listens for commands, responding based on the map's data.
     */
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
                String responseMessage = sendResponseUnicast(message);

                DatagramPacket response = new DatagramPacket(
                        responseMessage.getBytes(StandardCharsets.UTF_8),
                        responseMessage.length(),
                        packet.getAddress(),
                        packet.getPort()
                );

                socket.send(response);
            }
        } catch (Exception e) {
            System.out.println("There has been an issue while Unicast handle. " + e);
        }
    }

    /**
     * Main method to start multicast and unicast threads.
     *
     * @return Integer status code.
     */
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
            System.out.println("An issue has occurred when terminating threads. " + e);
            return 1;
        }
        return 0;
    }
}



