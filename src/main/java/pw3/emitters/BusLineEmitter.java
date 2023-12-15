package pw3.emitters;

import picocli.CommandLine;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@CommandLine.Command(
        name = "bus-line-emitter",
        description = "Start an UDP multicast emitter"
)

//faire javadoc à la place de simple comm
public class BusLineEmitter extends AbstractEmitter {

    protected String host = "239.0.0.1";

    @CommandLine.Option(
            names = {"-i", "--interface"},
            description = "Interface to use.",
            scope = CommandLine.ScopeType.INHERIT,
            required = true
    )
    private String interfaceName;

    @CommandLine.Option(
            names = {"-p", "--port"},
            description = "Port to use.",
            scope = CommandLine.ScopeType.INHERIT,
            required = true
    )
    private int port;

    private int lineNumber;
    private static final LinkedHashMap<String, Integer> busTrajectory = new LinkedHashMap<>();
    private final ArrayList<Bus> busRunningTheLine = new ArrayList<>();
    private String messagesToTransfer;

    public void setMessagesToTransfer(String s){
        messagesToTransfer = s;
    }

    public BusLineEmitter() {

    }


    public BusLineEmitter(int lNumber) {
        lineNumber = lNumber;
    }

    public synchronized void gatherUpdatesFromBuses() {
        messagesToTransfer  = "LINE " + lineNumber + " ";
        for (Bus bus : busRunningTheLine) {
            messagesToTransfer += bus.messageToLine;
            messagesToTransfer += " ";
        }
    }

    @Override
    public Integer call() {
        BusLineEmitter bl1 = new BusLineEmitter(101);
        BusLineEmitter bl2 = new BusLineEmitter(102);

        bl1.addingTrajectoryStop("BusStop 1", 0);
        bl1.addingTrajectoryStop("BusStop 2", 1000);
        bl1.addingTrajectoryStop("BusStop 3", 4000);
        bl1.addingTrajectoryStop("BusStop 5", 3000);

        bl2.addingTrajectoryStop("BusStop 1", 0);
        bl2.addingTrajectoryStop("BusStop 2", 5000);
        bl2.addingTrajectoryStop("BusStop 3", 2000);
        bl2.addingTrajectoryStop("BusStop 5", 3000);


        bl1.addingBusToLine(new Bus(3,10, bl1));
        bl1.addingBusToLine(new Bus(4,10, bl1));
        bl2.addingBusToLine(new Bus(1,10, bl2));
        bl2.addingBusToLine(new Bus(2,10, bl2));

        //les starts en threads
        bl1.getBus(0).start();
        bl1.getBus(1).start();
        bl2.getBus(0).start();
        bl2.getBus(1).start();

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

                    byte[] payload = bl1.messagesToTransfer.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket datagram = new DatagramPacket(
                            payload,
                            payload.length,
                            group
                    );
                    socket.send(datagram);

                    payload = bl2.messagesToTransfer.getBytes(StandardCharsets.UTF_8);
                    datagram = new DatagramPacket(
                            payload,
                            payload.length,
                            group
                    );
                    socket.send(datagram);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, delay, frequency, TimeUnit.MILLISECONDS);

            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

            socket.leaveGroup(group, networkInterface);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    // Allows to add a new bus stop at the end of the trajectory
    public void addingTrajectoryStop(String newStop, int timeFromLast) {
        if (busTrajectory.isEmpty()) {
            busTrajectory.put(newStop, 0);
        } else {
            busTrajectory.put(newStop, timeFromLast);
        }
    }

    public Map<String, Integer> getBusTrajectory(){
        return busTrajectory;
    }

    //insère après, pour l'instant on ne peut pas inserer en première place
    public void insertingTrajectoryStop(String newBusStop, int timeFromLast, String existingBusStop) {
        Map<String, Integer> newBusTrajectory = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : busTrajectory.entrySet()) {
            newBusTrajectory.put(entry.getKey(), entry.getValue());

            if (entry.getKey().equals(existingBusStop)) {
                newBusTrajectory.put(newBusStop, timeFromLast);
            }
        }
        busTrajectory.clear();
        busTrajectory.putAll(newBusTrajectory);
    }

    // Create a bus to add into the line
    public void addingBusToLine(Bus b) {
        busRunningTheLine.add(b);
    }

    // Delete a bus from the line
    public void removeBusFromLine(Bus b) {
        busRunningTheLine.remove(b);
    }

    //pe le chercher a partir de son id à la place de son indexe
    // get a specific bus
    public Bus getBus(int i) {
        return busRunningTheLine.get(i);
    }
}

