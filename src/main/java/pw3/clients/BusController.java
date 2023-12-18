package pw3.clients;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.*;

@Command(
        name = "bus-controller",
        description = "Start an UDP unicast client"
)
public class BusController implements Callable<Integer> {
    @CommandLine.Option(
            names = {"-H", "--host"},
            description = "Host to connect to.",
            scope = CommandLine.ScopeType.INHERIT,
            required = true
    )
    protected String host;

    @CommandLine.Option(
            names = {"-p", "--port"},
            description = "Port to use (default: 1234).",
            defaultValue = "1234",
            scope = CommandLine.ScopeType.INHERIT
    )
    protected int port;

    private final ConcurrentHashMap<String, String> lastTimeBusesWereSeen = new ConcurrentHashMap<>();

    private void showMenu() {
        System.out.println("===== Welcome on the bus controller =====");
        System.out.println("You can see a list of all buses :\t\t ALL");
        System.out.println("You can see a list of all buses of a line :\t LINE <n° line>");
        System.out.println("You can see a list of one bus of a line :\t BUS <n° line> <n° bus>");
    }

    @Override
    public Integer call() {

        showMenu();
        Scanner userCommand = new Scanner(System.in, StandardCharsets.UTF_8);

        while (true) {

        System.out.print("> ");
        String res = userCommand.nextLine().split(" ")[0].toUpperCase();

            try (DatagramSocket socket = new DatagramSocket()) {
                InetAddress serverAddress = InetAddress.getByName(host);

                String message = "GET_BUSES";

                byte[] payload = message.getBytes(StandardCharsets.UTF_8);

                DatagramPacket packet = new DatagramPacket(
                        payload,
                        payload.length,
                        serverAddress,
                        port
                );

                socket.send(packet);

                // Receive response from server
                byte[] buffer = new byte[1024];

                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String response = new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                String[] busInfo = response.split(",");

                for(int i = 0; i < busInfo.length; ++i) {
                    String[] bus = busInfo[i].split(" ");
                    String busName = bus[0];
                    String busState = bus[1] + bus[2];
                    lastTimeBusesWereSeen.put(busName, busState);
                }

            } catch (Exception e) {
                e.printStackTrace();
                return 1;
            }

            switch (res) {
                case "ALL":
                    for (String name: lastTimeBusesWereSeen.keySet()) {
                        String bus = name.toString();
                        String state[] = lastTimeBusesWereSeen.get(name).toString().split(" ");
                        if(bus.equals("601")) state[0] = "FIRE";
                        System.out.println("Bus : " + bus + "\t State : " + state[0]);
                        if(!state[0].equals("ALIVE")) System.out.print("\tBack in service at : " + state[1]);
                    }
                    break;
                case "LINE":
                    break;
                case "BUS":
                    break;
            }

            return 0;
        }
    }
}
