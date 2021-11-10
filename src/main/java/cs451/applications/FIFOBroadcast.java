package cs451.applications;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FIFOBroadcast extends BroadcastAbstraction{
    HashMap<String, Queue> _messages = new HashMap<String, Queue>();
    ConcurrentLinkedQueue<String> _logs;

    public FIFOBroadcast(int noProcesses, ConcurrentLinkedQueue<String> logs){
        _logs = logs;
        for (int i = 1; i < noProcesses + 1; i++){
            _messages.put(String.valueOf(i), new Queue());
        }
    }

    @Override
    public boolean broadcast(String uuid, String message) throws IOException {
        String sequenceId = Utils.getSequenceId(uuid);
        _logs.add("b " + sequenceId);
        return _belowBroadcastAbstraction.broadcast(uuid, message);
    }

    @Override
    public boolean deliver(String rawMessage) throws IOException {
        String messageHeader = Utils.getMessageHeader(rawMessage);
        String UUID = Utils.getUUID(messageHeader);
        String processId = Utils.getNonce(UUID);
        String sequenceId = Utils.getSequenceId(UUID);
        synchronized (_messages.get(processId)){
            List<String> messagesToDeliver = _messages.get(processId).updateReceivedMessages(Integer.parseInt(sequenceId), rawMessage);
            if (messagesToDeliver.size() > 0){
                for (String message : messagesToDeliver){
                    messageHeader = Utils.getMessageHeader(message);
                    UUID = Utils.getUUID(messageHeader);
                    processId = Utils.getNonce(UUID);
                    sequenceId = Utils.getSequenceId(UUID);
                    if (_aboveBroadcastAbstraction != null){
                        _aboveBroadcastAbstraction.deliver(message);
                    }
                    _logs.add("d " + processId + " " + sequenceId);
                }
                return true;
            }
        }
        return false;
    }

    class Queue {
        int _currentDelivered = 0;
        HashMap<Integer, String> _messages = new HashMap<>();

        public List<String> updateReceivedMessages(int sequenceId, String message){
            List<String> messagesToDeliver = new ArrayList<>();
            _messages.put(sequenceId, message);
            while (_messages.containsKey(_currentDelivered + 1)){
                messagesToDeliver.add(_messages.get(_currentDelivered + 1));
                _currentDelivered++;
            }
            return messagesToDeliver;
        }
    }
}
