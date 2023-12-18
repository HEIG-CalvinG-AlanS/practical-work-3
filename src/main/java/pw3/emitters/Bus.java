package pw3.emitters;

import picocli.CommandLine;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

@CommandLine.Command(
        name = "bus",
        description = "Start an UDP multicast emitter"
)

//faire javadoc à la place de simple comm
public class Bus extends AbstractEmitter {
    @CommandLine.Option(
            names = {"-H", "--host"},
            description = "Subnet range/multicast address to use.",
            scope = CommandLine.ScopeType.INHERIT,
            required = true
    )
    protected String host;

    @CommandLine.Option(
            names = {"-i", "--interface"},
            description = "Interface to use.",
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
    private int port;

    @CommandLine.Option(
            names = {"-n", "--name"},
            description = "Name/line of the bus",
            scope = CommandLine.ScopeType.INHERIT,
            required = true
    )
    private String lineName;

    @CommandLine.Option(
            names = {"-b", "--number"},
            description = "Number/number of the bus",
            scope = CommandLine.ScopeType.INHERIT,
            required = true
    )
    private int busNumber;
    BusState state = BusState.ALIVE;

    private Duration accidentDuration;
    LocalDateTime accidentEndTime = LocalDateTime.now();

    public enum BusState {
        FIRE(1), JAM(1), ACCIDENT(1), TIRE(1),ALIVE(0);
        private final int eventTime;
        BusState(int time) {
            this.eventTime = time;
        }
        public int getEventTime() {
            return eventTime;
        }
    }

    @Override
    public Integer call() {
        try (MulticastSocket socket = new MulticastSocket(port)) {
            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + port;
            System.out.println("Multicast emitter started (" + myself + ")");

            InetAddress multicastAddress = InetAddress.getByName(host);
            InetSocketAddress group = new InetSocketAddress(multicastAddress, port);
            NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
            socket.joinGroup(group, networkInterface);

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                try {

                    if(LocalDateTime.now().isAfter(accidentEndTime)) {
                        state = BusState.ALIVE;
                        accidentEndTime = LocalDateTime.now();
                        if (triggerEvent()) {
                            getRandomEvent();
                        }
                    }

                    String message = "LINE " + lineName + " BUS " + busNumber + " " + state + " " + accidentEndTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                    byte[] payload = message.getBytes(StandardCharsets.UTF_8);

                    DatagramPacket datagram = new DatagramPacket(
                            payload,
                            payload.length,
                            group
                    );

                    socket.send(datagram);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, delay, frequency, TimeUnit.MILLISECONDS);

            // Keep the program running for a while
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

            socket.leaveGroup(group, networkInterface);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }
    public boolean triggerEvent() {
        Random random = new Random();
        int randomNumber = random.nextInt(5) + 1;
        return randomNumber == 1;
    }

    public void getRandomEvent() throws InterruptedException {
        BusState[] states = BusState.values();
        Random random = new Random();
        state = states[random.nextInt(states.length - 2)]; //pour enlever le alive et le gas, qui sont gerés ailleur
        accidentDuration = Duration.ofMinutes(state.eventTime);
        LocalDateTime now = LocalDateTime.now();
        accidentEndTime = now.plus(accidentDuration);
    }
}