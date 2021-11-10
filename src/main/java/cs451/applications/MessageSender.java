package cs451.applications;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Timestamp;

public class MessageSender extends Thread {

    Channel _senderChannel;
    String _messageHeader;
    String _message;
    int _destId;
    InetAddress _destIp;
    int _destPort;

    public MessageSender(Channel senderChannel, String messageHeader, String message, int destId, InetAddress destIp, int destPort) {
        _senderChannel = senderChannel;
        _messageHeader = messageHeader;
        _message = message;
        _destId = destId;
        _destIp = destIp;
        _destPort = destPort;
    }

    @Override
    public void run() {
        try {
            _senderChannel.send(_messageHeader, _message, _destId, _destIp, _destPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
