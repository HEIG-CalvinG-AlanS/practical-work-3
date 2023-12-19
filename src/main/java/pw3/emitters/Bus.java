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

/**
 * This class represents a bus that emits UDP multicast messages.
 */
@CommandLine.Command(
        name = "bus",
        description = "Start an UDP multicast emitter"
)

public class Bus extends AbstractEmitter {

    /**
     * Multicast address or subnet range to use.
     */
    @CommandLine.Option(
            names = {"-H", "--host"},
            description = "Subnet range/multicast address to use.",
            scope = CommandLine.ScopeType.INHERIT,
            required = true
    )
    protected String host;


    /**
     * Interface to use for multicast communication.
     */
    @CommandLine.Option(
            names = {"-i", "--interface"},
            description = "Interface to use.",
            scope = CommandLine.ScopeType.INHERIT,
            required = true
    )
    protected String interfaceName;

    /**
     * Port to use for multicast (default: 9876).
     */
    @CommandLine.Option(
            names = {"-mp", "--multicast-port"},
            description = "Port to use for multicast (default: 9876).",
            defaultValue = "9876",
            scope = CommandLine.ScopeType.INHERIT
    )
    protected int port;

    /**
     * Name of the bus line
     */
    @CommandLine.Option(
            names = {"-n", "--name"},
            description = "Name/line of the bus",
            scope = CommandLine.ScopeType.INHERIT,
            required = true
    )
    protected String lineName;

    /**
     * Bus number.
     */
    @CommandLine.Option(
            names = {"-b", "--number"},
            description = "Number/number of the bus",
            scope = CommandLine.ScopeType.INHERIT,
            required = true
    )
    protected int busNumber;

    /**
     * Represents the state of the bus.
     */
    BusState state = BusState.ALIVE;

    /**
     * Time when the current accident ends.
     */
    LocalDateTime accidentEndTime = LocalDateTime.now();

    /**
     * Enumeration representing the possible states of the bus.
     */
    public enum BusState {
        FIRE(1), JAM(1), ACCIDENT(1), TIRE(1),ALIVE(0);
        /**
         * Time duration of the state in minutes.
         */
        private final int eventTime;

        /**
         * Constructs a BusState with the specified event time.
         *
         * @param time Time duration associated with the state.
         */
        BusState(int time) {
            this.eventTime = time;
        }
    }

    /**
     * Starts the UDP multicast emitter.
     *
     * @return 0 on success, 1 on failure.
     */
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
                }
            }, delay, frequency, TimeUnit.MILLISECONDS);

            // Keep the program running for a while
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            socket.leaveGroup(group, networkInterface);
        } catch (Exception e) {
            System.out.println("There has been an issue while sending the message. " + e);
            return 1;
        }
        return 0;
    }

    /**
     * Has a chance to trigger a random event
     *
     * @return true if an event is triggered, false otherwise.
     */
    public boolean triggerEvent(){
        Random random = new Random();
        int randomNumber = random.nextInt(5) + 1;
        return randomNumber == 1;
    }

    /**
     * Gets a random event, updates the bus state and set the waiting time.
     *
     */
    public void getRandomEvent(){
        BusState[] states = BusState.values();
        Random random = new Random();
        state = states[random.nextInt(states.length - 1)]; // -1 because Alive is not an unusual state
        Duration accidentDuration = Duration.ofMinutes(state.eventTime);
        LocalDateTime now = LocalDateTime.now();

        // the time in which the bus will go back to the ALIVE state
        accidentEndTime = now.plus(accidentDuration);
    }
}