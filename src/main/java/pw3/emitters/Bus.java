package pw3.emitters;

import java.util.Map;
import java.util.Random;

public class Bus extends Thread {
    private final int busNumber;
    private double gasolineAmount;
    private final double RESERVOIRE_SIZE;
    private static final int TIME_TO_REFILL = 5000;

    String messageToLine;
    BusState state;
    BusLineEmitter owner;
    private int busDelay;


    public enum BusState {
        FIRE(8000), JAM(6000), ACCIDENT(9000), TIRE(7000), GAS(TIME_TO_REFILL), ALIVE(0);
        private final int eventTime;

        BusState(int time) {
            this.eventTime = time;
        }

        public int getEventTime() {
            return eventTime;
        }
    }

    public Bus(int bNumber, double rSize, BusLineEmitter b) {
        busNumber = bNumber;
        RESERVOIRE_SIZE = rSize;
        busDelay = 0;
        gasolineAmount = RESERVOIRE_SIZE;
        state = BusState.ALIVE;
        messageToLine = "";
        owner = b;
    }

    // refill the gas tank
    public void reFillingGas() throws InterruptedException {
        busDelay += TIME_TO_REFILL;
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
                    state = BusState.GAS;
                    messageToLine = "BUS " + busNumber + " " + state + " " + busDelay;
                    owner.gatherUpdatesFromBuses();
                    reFillingGas();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            busDelay = 0;

            for (Map.Entry<String, Integer> entry : owner.getBusTrajectory().entrySet()) {
                try {
                    sleep(entry.getValue());

                    reducingGasoline(entry.getValue());

                    if (triggerEvent()) {
                        messageToLine = getRandomEvent();
                        owner.gatherUpdatesFromBuses();
                        waitEndEvent();
                    }

                    if (gasolineAmount <= 0) {
                        messageToLine = "BUS " + busNumber + " " + state + " " + busDelay;
                        owner.gatherUpdatesFromBuses();
                        reFillingGas();
                    }
                    state = BusState.ALIVE;
                    messageToLine = "BUS " + busNumber + " " + state + " " + busDelay;
                    owner.gatherUpdatesFromBuses();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            messageToLine = "BUS" + busNumber + " end of course, delay of " + busDelay;
            owner.gatherUpdatesFromBuses();
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

        busDelay += state.getEventTime();
        return "BUS " + busNumber + " " + state + " " + busDelay;
    }
}