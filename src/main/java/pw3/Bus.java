package pw3;

import java.util.*;

import static java.lang.Thread.sleep;


public class Bus {
    private final int busNumber;
    private double gasolineAmount;
    private final double RESERVOIRE_SIZE;
    private final int TIME_TO_REFILL = 5;
    private LinkedHashMap<String, Integer> busTrajectory = new LinkedHashMap<>();
    private boolean communicationIssue; //peut etre enlever, trouver une autre manière?
    private int delay;

    Bus(int bNumber, double rSize) {
        busNumber = bNumber;
        RESERVOIRE_SIZE = rSize;
        communicationIssue = false;
        delay = 0;
        gasolineAmount = RESERVOIRE_SIZE;
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

    public void displayBusTrajectory() {
        if (busTrajectory.isEmpty())
            return;

        System.out.println("Bus Trajectory:");
        for (Map.Entry<String, Integer> entry : busTrajectory.entrySet()) {
            System.out.println("Stop: " + entry.getKey() + ", Time: " + entry.getValue() + " ms");
        }
    }


    public void reFillingGas() throws InterruptedException {
        delay += TIME_TO_REFILL;
        sleep(TIME_TO_REFILL);
        gasolineAmount = RESERVOIRE_SIZE;
    }

    public static boolean forgetToRefill() {
        Random random = new Random();
        int forgets = random.nextInt(6);
        return forgets == 0;
    }

    public void reducingGasoline(double time){
        gasolineAmount -= time /1000/2;
    }


    public void start() throws InterruptedException {

        if (!forgetToRefill()) {
            System.out.println("Refilling at the beginning");
            reFillingGas();
        }

        delay = 0;

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

