package cs451.applications;

import java.io.IOException;
import java.net.InetAddress;

public class MessageSender extends Thread {

    Channel _senderChannel;
    String _uuid;
    String _message;
    InetAddress _destIp;
    int _destPort;

    public MessageSender(Channel senderChannel, String uuid, String message, InetAddress destIp, int destPort) {
        _senderChannel = senderChannel;
        _uuid = uuid;
        _message = message;
        _destIp = destIp;
        _destPort = destPort;
    }

    @Override
    public void run() {
        try {
            _senderChannel.send(_uuid, _message, _destIp, _destPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finished sending -------  " + _uuid + "-----------");
    }
}
