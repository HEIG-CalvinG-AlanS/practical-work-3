package pw3;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class Bus {
    private final int busNumber;
    private double essenceAmount;
    private static double RESERVOIRE_SIZE;
    private LinkedHashMap<String, Integer> busTraject = new LinkedHashMap<>();
    private boolean communicationIssue; //peut etre enlever, trouver une autre manière?
    private int delay;

    Bus(int bNumber, double rSize){
        busNumber = bNumber;
        RESERVOIRE_SIZE = rSize;
        communicationIssue = false;
        delay = 0;
    }
}
