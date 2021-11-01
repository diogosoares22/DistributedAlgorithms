package cs451.applications;

import cs451.Host;
import cs451.Parser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StubbornLinks extends Channel {

    ConcurrentLinkedQueue<String> _logs;
    Parser _parser;
    List<ConcurrentHashMap<String, String>> _ackDB = new ArrayList<>();

    public StubbornLinks(ConcurrentLinkedQueue<String> logs, Parser parser) {
        super();
        _logs = logs;
        _parser = parser;
        int hostNumber = parser.hosts().size();
        for (int i = 0; i < hostNumber + 1; i++){
            _ackDB.add(new ConcurrentHashMap<String, String>());
        }
    }

    @Override
    public boolean send(String uuid, String rawMessage, InetAddress destIp, int destPort) throws IOException{
        int processId = Integer.parseInt(Utils.getProcessId(uuid));
        while (!_ackDB.get(processId).containsKey(uuid)) {
            _belowChannel.send(uuid, rawMessage, destIp, destPort);
        }
        return true;
    }

    @Override
    public boolean deliver(String rawMessage) throws IOException {
        String uuid = Utils.getUUID(rawMessage);
        String message = Utils.getMessage(rawMessage);
        int processId = Integer.parseInt(Utils.getProcessId(uuid));

        if (!message.equals(Constants.CONFIRMATION)){
            Host host = _parser.getHostById(processId);
            String confirmMessage = Utils.addHeader(uuid, Constants.CONFIRMATION);
            Utils.sendUdpMessage(confirmMessage, InetAddress.getByName(host.getIp()), host.getPort());
            if (_aboveChannel != null) {
                return _aboveChannel.deliver(rawMessage);
            }
        }
        else {
            _ackDB.get(processId).putIfAbsent(uuid, rawMessage);
        }
        return true;
    }

}
