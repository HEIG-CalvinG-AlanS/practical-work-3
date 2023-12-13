package pw3.emitters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static java.lang.Thread.sleep;

//faire javadoc à la place de simple comm
public class BusLine {
    private int lineNumber;
    private static final LinkedHashMap<String, Integer> busTrajectory = new LinkedHashMap<>();
    private final ArrayList<Bus> busRunningTheLine = new ArrayList<>();

    public BusLine(int lNumber) {
        lineNumber = lNumber;
    }

    // Allows to add a new bus stop at the end of the trajectory
    public void addingTrajectoryStop(String newStop, int timeFromLast) {
        if (busTrajectory.isEmpty()) {
            busTrajectory.put(newStop, 0);
        } else {
            busTrajectory.put(newStop, timeFromLast);
        }
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

    //maybe remove this, used for tests
    public void displayBusTrajectory() {
        if (busTrajectory.isEmpty())
            return;

        System.out.println("Line Trajectory:");
        for (Map.Entry<String, Integer> entry : busTrajectory.entrySet()) {
            System.out.println("Stop name: " + entry.getKey() + ", time: " + entry.getValue());
        }
    }

    // return every bus stop from the line
    public Map<String, Integer> getBusStops() {
        return busTrajectory;
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


    public static class Bus extends Thread {
        private final int busNumber;
        private double gasolineAmount;
        private final double RESERVOIRE_SIZE;
        private static final int TIME_TO_REFILL = 5000;

        String messageTemplate = "BUS %d %s %d";

        private String datagram;

        BusState state;
        private int delay;


        public enum BusState {
            FIRE(8000), JAM(3000), ACCIDENT(9000), TIRE(7000), GAS(TIME_TO_REFILL), ALIVE(0);

            private final int eventTime;

            BusState(int time) {
                this.eventTime = time;
            }

            public int getEventTime() {
                return eventTime;
            }
        }

        public Bus(int bNumber, double rSize) {
            busNumber = bNumber;
            RESERVOIRE_SIZE = rSize;
            delay = 0;
            gasolineAmount = RESERVOIRE_SIZE;
            state = BusState.ALIVE;
        }

        // refill the gas tank
        public void reFillingGas() throws InterruptedException {
            delay += TIME_TO_REFILL;
            sleep(TIME_TO_REFILL);
            gasolineAmount = RESERVOIRE_SIZE;
        }

        // low chance in which the gas tank wasn't refilled at the beginning of the course
        public static boolean forgetToRefill() {
            Random random = new Random();
            int forgets = random.nextInt(6);
            return forgets == 0;
        }

        //reduces the amount of gasoline left in the tank proportionnaly to the distance run
        public void reducingGasoline(double time) {
            gasolineAmount -= time / 1000 / 2;
        }

        @Override
        public void run() {
            while (true) {

                if (!forgetToRefill()) {
                    try {
                        System.out.println("BUS " + busNumber + " refilling at the beginning");
                        reFillingGas();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                delay = 0;

                for (Map.Entry<String, Integer> entry : busTrajectory.entrySet()) {
                    try {
                        sleep(entry.getValue());

                        reducingGasoline(entry.getValue());

                        if (triggerEvent()) {
                            System.out.println(getRandomEvent());
                            waitEndEvent();
                        }

                        if (gasolineAmount <= 0) {
                            System.out.println("BUS " + busNumber + " " + state + " " + delay);
                            reFillingGas();
                        }
                        state = BusState.ALIVE;
                        System.out.println("BUS " + busNumber + " " + state + " " + delay);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("BUS" + busNumber + " end of course, delay of " + delay);
            }
        }

        public void waitEndEvent() throws InterruptedException {
            sleep(state.getEventTime());
        }

        public boolean triggerEvent() {
            Random random = new Random();
            int randomNumber = random.nextInt(5) + 1;

            return randomNumber == 1;
        }

        public String getRandomEvent() throws InterruptedException {
            BusState[] states = BusState.values();
            Random random = new Random();
            state = states[random.nextInt(states.length - 2)]; //pour enlever le alive et le gas, qui sont gerés ailleur

            delay += state.getEventTime();
            return "BUS " + busNumber + " " + state + " " + delay;
        }
    }
}



