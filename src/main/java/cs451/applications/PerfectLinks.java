package cs451.applications;

import cs451.Parser;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PerfectLinks extends Channel {

    ConcurrentHashMap<String, String> _messageDB;
    BestEffortBroadcast _bestEffortBroadcast;

    public PerfectLinks(ConcurrentHashMap<String, String> messageDB) {
        super();
        _messageDB = messageDB;
    }

    public void setBestEffortBroadcast(BestEffortBroadcast bestEffortBroadcast){
        _bestEffortBroadcast = bestEffortBroadcast;
    }

    @Override
    public boolean send(String messageHeader, String message, int destId, InetAddress destIp, int destPort) throws IOException {
        return _belowChannel.send(messageHeader, message, destId, destIp, destPort);
    }

    @Override
    public boolean deliver(String rawMessage) throws IOException {
        String messageHeader = Utils.getMessageHeader(rawMessage);
        if (_messageDB.putIfAbsent(messageHeader, rawMessage) == null){
            if (_bestEffortBroadcast != null){
                return _bestEffortBroadcast.deliver(rawMessage);
            }
            return true;
        }
        return false;
    }
}
