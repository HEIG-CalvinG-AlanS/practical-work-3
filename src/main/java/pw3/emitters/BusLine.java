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


    public static class Bus {
        private final int busNumber;
        private double gasolineAmount;
        private final double RESERVOIRE_SIZE;
        private final int TIME_TO_REFILL = 5;

        private boolean communicationIssue; //peut etre enlever, trouver une autre manière?
        private int delay;

        public Bus(int bNumber, double rSize) {
            busNumber = bNumber;
            RESERVOIRE_SIZE = rSize;
            communicationIssue = false;
            delay = 0;
            gasolineAmount = RESERVOIRE_SIZE;
        }

        // refille the gas tank
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

        //pe utiliser threads pour commencer le parcours
        public void start() throws InterruptedException {
            delay = 0;

            if (!forgetToRefill()) {
                System.out.println("Refilling at the beginning");
                reFillingGas();
            }

            for (Map.Entry<String, Integer> entry : busTrajectory.entrySet()) {
                sleep(entry.getValue());
                reducingGasoline(entry.getValue());

                if (gasolineAmount > 0) {
                    System.out.println("Bus " + busNumber + " reached " + entry.getKey() + " gasoline: " + gasolineAmount);
                } else {
                    System.out.println("No more gas left. Refilling...");
                    reFillingGas();
                    System.out.println("Bus " + busNumber + " reached " + entry.getKey() + " gasoline: " + gasolineAmount);
                }
            }
            System.out.println("End of course, delay of " + delay);
            delay = 0;
        }
    }
}
