package cs451.applications;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FairLossLinks extends Channel{

    public FairLossLinks(){
        super();
    }

    @Override
    public boolean send(String messageHeader, String rawMessage, int destId, InetAddress destIp, int destPort) throws IOException {

        String message = Utils.addMessageHeader(messageHeader, rawMessage);

        Utils.sendUdpMessage(message, destIp, destPort);

        return true;
    }

    @Override
    public boolean deliver(String rawMessage) throws IOException {
        if (_aboveChannel != null) {
            return _aboveChannel.deliver(rawMessage);
        }
        return true;
    }
}
