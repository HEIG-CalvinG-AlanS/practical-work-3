package pw3;

import pw3.emitters.BusLine;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        BusLine bl1 = new BusLine(101);
        BusLine bl2 = new BusLine(102);

        bl1.addingTrajectoryStop("BusStop 1", 0);
        bl1.addingTrajectoryStop("BusStop 2", 1000);
        bl1.addingTrajectoryStop("BusStop 3", 4000);
        bl1.addingTrajectoryStop("BusStop 5", 3000);

        bl1.displayBusTrajectory();
        bl2.displayBusTrajectory();

        bl2.addingTrajectoryStop("BusStop 1", 3);
        bl2.displayBusTrajectory();

        bl1.insertingTrajectoryStop("BusStop 4", 7000, "BusStop 3");
        bl1.displayBusTrajectory();


        bl1.addingBusToLine(new BusLine.Bus(0,10));
        bl1.addingBusToLine(new BusLine.Bus(1,10));

        //les starts en threads
        bl1.getBus(0).start();
        bl1.getBus(1).start();

    }
}