package pw3;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import pw3.emitters.BusController;
import pw3.emitters.BusLineEmitter;
import pw3.receivers.BusStation;

import static java.lang.Thread.sleep;

@Command(
        description = "Practical content of the Java UDP programming chapter",
        version = "1.0.0",
        subcommands = {
                BusLineEmitter.class,
                BusStation.class,
                BusController.class
        },
        scope = CommandLine.ScopeType.INHERIT,
        mixinStandardHelpOptions = true
)

public class Main {
    public static void main(String... args) throws InterruptedException {

        BusLineEmitter bl1 = new BusLineEmitter(101);
        BusLineEmitter bl2 = new BusLineEmitter(102);

        bl1.addingTrajectoryStop("BusStop 1", 0);
        bl1.addingTrajectoryStop("BusStop 2", 1000);
        bl1.addingTrajectoryStop("BusStop 3", 4000);
        bl1.addingTrajectoryStop("BusStop 5", 3000);


        bl2.addingTrajectoryStop("BusStop 1", 3);

        bl1.insertingTrajectoryStop("BusStop 4", 7000, "BusStop 3");


        bl1.addingBusToLine(new BusLineEmitter.Bus(3,10));
        bl1.addingBusToLine(new BusLineEmitter.Bus(7,10));

        //les starts en threads
        bl1.getBus(0).start();
        sleep(10000);
        bl1.getBus(1).start();

        // Source: https://stackoverflow.com/a/11159435
        String commandName = new java.io.File(
                Main.class.getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .getPath()
        ).getName();

        int exitCode = new CommandLine(new Main())
                .setCommandName(commandName)
                .execute(args);
        System.exit(exitCode);
    }
}