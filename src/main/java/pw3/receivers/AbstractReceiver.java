package pw3.receivers;

import picocli.CommandLine;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public abstract class AbstractReceiver implements Callable<Integer> {

}