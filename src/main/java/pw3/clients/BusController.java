package pw3.clients;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.Callable;

/**
 * Represents a UDP unicast client for controlling and querying bus information.
 * This class provides a command-line interface for users to send specific commands
 * to a server and receive information about buses.
 */
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

    /**
     * Displays the menu options to the user.
     * This method prints out the available commands that users can send to the server.
     */
    private void showMenu() {
        System.out.println("===== Welcome on the bus controller =====");
        System.out.println("You can see a list of all buses :\t\tALL");
        System.out.println("You can see a list of all buses of a line :\tLINE <n° line>");
        System.out.println("You can see a list of one bus of a line :\tBUS <n° line> <n° bus>");
    }

    /**
     * The main execution method for the bus controller client.
     * This method reads user input, sends the appropriate commands to the server via UDP,
     * and processes the server's responses.
     *
     * @return Integer status code.
     */
    @Override
    public Integer call() {
        showMenu();
        Scanner userCommand = new Scanner(System.in, StandardCharsets.UTF_8);
        String[] res;
        String[] user = new String[3];
        user[0] = "";

        while (true) {

            user[0] = ""; // Command
            user[1] = ""; // Line of bus
            user[2] = ""; // Numero of bus

            while (!user[0].equals("ALL") && !user[0].equals("LINE") &&
                    !user[0].equals("BUS")) {
                System.out.print("> ");
                res = userCommand.nextLine().split(" ");
                user[0] = res[0].toUpperCase();
                if((user[0].equals("LINE") || user[0].equals("BUS")) && res.length >= 2) user[1] = res[1];
                if(user[0].equals("BUS") && res.length >= 3) user[2] = res[2];
            }

            try (DatagramSocket socket = new DatagramSocket()) {
                InetAddress serverAddress = InetAddress.getByName(host);

                String message = user[0];
                if(user[1] != null) message += " " + user[1];
                if(user[2] != null) message += " " + user[2];

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

                // Separate each bus in a table
                String[] busInfo = response.split(",");

                // Check if it's an error
                String busInfoError = busInfo[0].split(" ")[0];

                // Remove "OK" of the protocol (useless if error)
                busInfo[0] = busInfo[0].substring(3);

                // Add to the map each bus if at least one in circulation
                if(busInfoError.equals("ERROR")) {
                    if(busInfo[0].split(" ")[1].equals("LINE"))
                        System.out.println("No information on the line requested.");
                    else
                        System.out.println("No information on the bus requested.");
                }
                else {
                    for (int i = 0; i < busInfo.length; ++i) {
                        String[] bus = busInfo[i].split(" ");
                        System.out.print("Line : " + bus[1] + "\tBus : " + bus[3] + "\t State : " + bus[4]);
                        if (!bus[4].equals("ALIVE")) System.out.print("\tBack in service at : " + bus[5] + "\n");
                        else System.out.print("\n");
                    }
                }

            } catch (Exception e) {
                System.out.println("There has been an issue while Unicast handle. " + e);
                return 1;
            }
        }
    }
}