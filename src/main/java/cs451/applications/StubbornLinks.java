package cs451.applications;

import cs451.Host;
import cs451.Parser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StubbornLinks extends Channel {

    ConcurrentLinkedQueue<String> _logs;
    Parser _parser;
    ConcurrentHashMap<String, String> _ackDB;

    public StubbornLinks(ConcurrentLinkedQueue<String> logs, Parser parser, ConcurrentHashMap<String, String> ackDB) {
        super();
        _logs = logs;
        _parser = parser;
        _ackDB = ackDB;
    }

    @Override
    public boolean send(String uuid, String rawMessage, InetAddress destIp, int destPort) throws IOException{
        while (!_ackDB.containsKey(uuid)) {
            _belowChannel.send(uuid, rawMessage, destIp, destPort);
        }
        return true;
    }

    @Override
    public boolean deliver(String rawMessage) throws IOException {
        String uuid = Utils.getUUID(rawMessage);
        String message = Utils.getMessage(rawMessage);

        if (!message.equals(Constants.CONFIRMATION)){
            int processId = Integer.parseInt(Utils.getProcessId(uuid));
            Host host = _parser.getHostById(processId);
            String confirmMessage = Utils.addHeader(uuid, Constants.CONFIRMATION);
            Utils.sendUdpMessage(confirmMessage, InetAddress.getByName(host.getIp()), host.getPort());
        }
        else {
            if (!_ackDB.containsKey(uuid)) {_ackDB.put(uuid, rawMessage);}
            return true;
        }

        if (_aboveChannel != null) {
            return _aboveChannel.deliver(rawMessage);
        }
        return true;
    }

}
