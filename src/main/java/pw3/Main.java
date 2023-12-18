package pw3;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import pw3.clients.BusController;
import pw3.emitters.Bus;
import pw3.receivers.BusStation;

import static java.lang.Thread.sleep;

@Command(
        description = "Practical content of the Java UDP programming chapter",
        version = "1.0.0",
        subcommands = {
                Bus.class,
                BusStation.class,
                BusController.class
        },
        scope = CommandLine.ScopeType.INHERIT,
        mixinStandardHelpOptions = true
)

public class Main {
    public static void main(String... args) {
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