package pw3;

import pw3.emitters.BusLine;

import static java.lang.Thread.sleep;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        BusLine bl1 = new BusLine(101);
        BusLine bl2 = new BusLine(102);

        bl1.addingTrajectoryStop("BusStop 1", 0);
        bl1.addingTrajectoryStop("BusStop 2", 1000);
        bl1.addingTrajectoryStop("BusStop 3", 4000);
        bl1.addingTrajectoryStop("BusStop 5", 3000);


        bl2.addingTrajectoryStop("BusStop 1", 3);

        bl1.insertingTrajectoryStop("BusStop 4", 7000, "BusStop 3");


        bl1.addingBusToLine(new BusLine.Bus(3,10));
        bl1.addingBusToLine(new BusLine.Bus(7,10));

        //les starts en threads
        bl1.getBus(0).start();
        sleep(3000);
        bl1.getBus(1).start();

    }
}