package pw3.clients;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

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

    @Override
    public Integer call() {

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

            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }
}
