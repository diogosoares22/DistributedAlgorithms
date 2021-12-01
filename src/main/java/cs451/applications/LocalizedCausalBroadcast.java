package cs451.applications;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LocalizedCausalBroadcast extends BroadcastAbstraction{

    int _myId;
    List<Integer> _affectedProcesses; // this should include the trivial problem of own ordering

    List<Integer> _senderVectorClock = new ArrayList<>();
    List<Integer> _receiverVectorClock = new ArrayList<>();

    ConcurrentLinkedQueue<MessageWithVectorClock> _pendingMessages = new ConcurrentLinkedQueue<>();
    ConcurrentLinkedQueue<String> _logs;


    public LocalizedCausalBroadcast(int myId, List<Integer> affectedProcesses, int noProcesses, ConcurrentLinkedQueue<String> logs){
        _myId = myId;
        _affectedProcesses = affectedProcesses;
        _logs = logs;
        for (int i = 0; i < noProcesses + 1; i++){
            _senderVectorClock.add(0);
            _receiverVectorClock.add(0);
        }
    }


    @Override
    public boolean broadcast(String uuid, String message) throws IOException {
        List<Integer> vectorDependencies;
        String sequenceId = Utils.getSequenceId(uuid);

        synchronized (_senderVectorClock) {
            _logs.add("b " + sequenceId);
            _senderVectorClock.set(_myId, _senderVectorClock.get(_myId) + 1);
            vectorDependencies = getVectorDependencies();
        }
        return _belowBroadcastAbstraction.broadcast(Utils.addVectorClock(uuid, vectorDependencies), message); // need to send current vector clock, with non affectedProcesses with 0s
    }

    @Override
    public boolean deliver(String rawMessage) throws IOException {
        String messageHeader = Utils.getMessageHeader(rawMessage);
        String UUID = Utils.getUUID(messageHeader);
        List<Integer> messageVectorClock = Utils.getVectorClock(UUID);

        System.out.println("\nReceived Message with VectorClock: ");
        System.out.println(messageVectorClock);

        MessageWithVectorClock messageWithVectorClock = new MessageWithVectorClock(messageVectorClock, rawMessage);

        _pendingMessages.add(messageWithVectorClock);

        tryDeliverPendingMessages();

        return false;
    }

    public synchronized void tryDeliverPendingMessages() throws IOException{
        // see if pending messages can be delivered
        boolean iterate = true;
        while (iterate) {
            for (MessageWithVectorClock value : _pendingMessages) {
                iterate = processMessageVectorClock(value);
                if (iterate) {
                    String currUUID = Utils.getUUID(value._message);
                    int processId = Integer.parseInt(Utils.getNonce(currUUID));
                    int sequenceId = Integer.parseInt(Utils.getSequenceId(currUUID));

                    if (_aboveBroadcastAbstraction != null) {
                        _aboveBroadcastAbstraction.deliver(value._message);
                    }

                    System.out.println("\nDelivered Message with VectorClock: ");
                    System.out.println(value._vectorClock);

                    _logs.add("d " + processId + " " + sequenceId);

                    _pendingMessages.remove(value);
                    break;
                }
            }
            iterate = false;
        }
    }

    public boolean processMessageVectorClock(MessageWithVectorClock value){
        if (canDeliver(value._vectorClock, value._processId)) {
            int processId = value._processId;

            if (processId != _myId) _senderVectorClock.set(processId, _senderVectorClock.get(processId) + 1);
            _receiverVectorClock.set(processId, _receiverVectorClock.get(processId) + 1);
            return true;
        }
        return false;
    }

    public List<Integer> getVectorDependencies(){
        List<Integer> vectorDependencies = new ArrayList<>(Collections.nCopies(_senderVectorClock.size(), 0));
        for (int processId : _affectedProcesses){
            vectorDependencies.set(processId, _senderVectorClock.get(processId));
        }
        return vectorDependencies;
    }

    public boolean canDeliver(List<Integer> messageVectorClock, int origin){
        for (int i = 0; i < messageVectorClock.size(); i++){
            if ((i != origin) & (messageVectorClock.get(i) > _receiverVectorClock.get(i))){
                return false;
            }
        }
        return messageVectorClock.get(origin) == _receiverVectorClock.get(origin) + 1;
    }

    class MessageWithVectorClock {
        List<Integer> _vectorClock;
        int _processId;
        String _message;

        public MessageWithVectorClock(List<Integer> vectorClock, String message){
            _vectorClock = vectorClock;
            _message = message;
            _processId = Integer.parseInt(Utils.getNonce(Utils.getMessageHeader(message)));
        }

    }
}
