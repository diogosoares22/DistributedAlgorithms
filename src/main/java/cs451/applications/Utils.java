package cs451.applications;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.Timestamp;

public class Utils {

    public static String addHeader(String uuid, String message){
        return uuid + "/" + message;
    }

    public static String getUUID(String rawMessage){
        String[] arr = rawMessage.split("/", 2);
        return arr[0];
    }

    public static String createUUID(String processId){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return processId + "@" + timestamp.toString();
    }

    public static String getProcessId(String UUID){
        String[] arr = UUID.split("@", 2);
        return arr[0];
    }

    public static String getMessage(String rawMessage){
        String[] arr = rawMessage.split("/", 2);
        return arr[1];
    }

    /** code based on https://www.baeldung.com/udp-in-java **/
    public static void sendUdpMessage(String message, InetAddress destIp, int destPort) throws IOException {

        byte [] buf = message.getBytes();
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, destIp, destPort);
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);

        socket.close();
    }

    public static String receiveUdpMessage(int port, DatagramSocket socket) throws SocketException {
        byte[] buf = new byte[256];


        if (socket == null){
            socket = new DatagramSocket(port);
        }
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        socket.close();

        return new String(packet.getData(), 0, packet.getLength());
    }
}
