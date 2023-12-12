package pw3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.Thread.sleep;


public class Bus {
    private final int busNumber;
    private double essenceAmount;
    private static double RESERVOIRE_SIZE;
    private LinkedHashMap<String, Integer> busTrajectory = new LinkedHashMap<>();
    private boolean communicationIssue; //peut etre enlever, trouver une autre manière?
    private int delay;

    Bus(int bNumber, double rSize) {
        busNumber = bNumber;
        RESERVOIRE_SIZE = rSize;
        communicationIssue = false;
        delay = 0;
        essenceAmount = RESERVOIRE_SIZE;
    }

    // Allows to add a new bus stop at the end of the trajectory
    public void addingTrajectoryStop(String b, int timeFromLast) {
        if (busTrajectory.isEmpty()) {
            busTrajectory.put(b, 0);
        } else {
            busTrajectory.put(b, timeFromLast);
        }

    }

    public void reFillingGas() throws InterruptedException {
        //sleep(5);
        essenceAmount = RESERVOIRE_SIZE;
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

    public void start() throws InterruptedException {
        for (Map.Entry<String, Integer> entry : busTrajectory.entrySet()) {
            sleep(entry.getValue());
            essenceAmount--;
            if(essenceAmount > 0) {
                System.out.println("Bus " + busNumber + " reached " + entry.getKey());
            }else{
                System.out.println("No more gas left");
            }
        }
    }
}

