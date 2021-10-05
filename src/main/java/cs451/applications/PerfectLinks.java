package cs451.applications;

import cs451.Parser;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PerfectLinks extends Channel {

    ConcurrentLinkedQueue<String> _logs;
    ConcurrentHashMap<String, String> _messageDB;

    public PerfectLinks(ConcurrentLinkedQueue<String> logs, ConcurrentHashMap<String, String> messageDB) {
        super();
        _logs = logs;
        _messageDB = messageDB;
    }

    @Override
    public boolean send(String uuid, String message, InetAddress destIp, int destPort) throws IOException {
        _logs.add("b " + message);
        return _belowChannel.send(uuid, message, destIp, destPort);
    }

    @Override
    public boolean deliver(String rawMessage) throws IOException {
        String uuid = Utils.getUUID(rawMessage);
        if (!_messageDB.containsKey(uuid)){
            System.out.println("Received message  ---- " + rawMessage + " ---- ...\n");
            _messageDB.put(uuid, rawMessage);
            _logs.add("d " + Utils.getProcessId(uuid) + " " + Utils.getMessage(rawMessage));
            return true;
        }
        return false;
    }
}
