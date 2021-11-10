package cs451.applications;

import cs451.Host;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class UniformReliableBroadcast extends BroadcastAbstraction{

    int _noProcesses;
    String _processId;
    ConcurrentHashMap<String, HashSet<String>> _messages = new ConcurrentHashMap<String, HashSet<String>>();
    ConcurrentHashMap<String, String> _deliveredMessages = new ConcurrentHashMap<>();

    public UniformReliableBroadcast(int noProcesses, String processId) {
        super();
        _noProcesses = noProcesses;
        _processId = processId;
    }

    @Override
    public boolean broadcast(String uuid, String message) throws IOException {
        return _belowBroadcastAbstraction.broadcast(uuid, message);
    }

    @Override
    public boolean deliver(String rawMessage) throws IOException {
        String messageHeader = Utils.getMessageHeader(rawMessage);
        String processId = Utils.getProcessId(messageHeader);
        String UUID = Utils.getUUID(messageHeader);
        String message = Utils.getMessage(rawMessage);

        HashSet<String> currentSet = _messages.compute(UUID, (key, value) -> {if (value == null){ return new HashSet<String>(List.of(processId));}
                                                                              else{value.add(processId);} return value;});
        int confirmedProcesses = currentSet.size();
        if (confirmedProcesses == 1){
            _belowBroadcastAbstraction.broadcast(UUID, message);
        }
        else if ((confirmedProcesses > _noProcesses / 2) & (_deliveredMessages.putIfAbsent(UUID, message) == null)){
            if (_aboveBroadcastAbstraction != null) {
                return _aboveBroadcastAbstraction.deliver(rawMessage);
            }
            return true;
        }
        return false;
    }
}
