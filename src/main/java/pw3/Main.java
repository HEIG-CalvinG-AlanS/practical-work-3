package pw3;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Bus b1 = new Bus(101, 15.5);
        Bus b2 = new Bus(102, 10.5);

        b1.addingTrajectoryStop("BusStop 1", 0);
        b1.addingTrajectoryStop("BusStop 2", 1000);
        b1.addingTrajectoryStop("BusStop 3", 4000);
        b1.addingTrajectoryStop("BusStop 5", 3000);

        b1.displayBusTrajectory();
        b2.displayBusTrajectory();

        b2.addingTrajectoryStop("BusStop 1", 3);
        b2.displayBusTrajectory();

        b1.insertingTrajectoryStop("BusStop 4", 7000, "Arret 3");
        b1.displayBusTrajectory();

        b1.start();
        b1.start();
        b1.start();
        b1.start();
        b1.start();
    }
}