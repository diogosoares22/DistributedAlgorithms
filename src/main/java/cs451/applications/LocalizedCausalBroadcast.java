package cs451.applications;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LocalizedCausalBroadcast extends BroadcastAbstraction{

    int _myId;
    List<Integer> _affectedProcesses; // this should include the trivial problem of own ordering
    List<Integer> _currentVectorClock = new ArrayList<>();
    ConcurrentHashMap<String, MessageWithVectorClock> _pendingMessages = new ConcurrentHashMap<>();

    public LocalizedCausalBroadcast(int myId, List<Integer> affectedProcesses, int noProcesses){
        _myId = myId;
        _affectedProcesses = affectedProcesses;
        for (int i = 0; i < noProcesses + 1; i++){
            _currentVectorClock.add(0);
        }
    }


    @Override
    public boolean broadcast(String uuid, String message) throws IOException {
        synchronized (_currentVectorClock){
            _currentVectorClock.set(_myId, _currentVectorClock.get(_myId) + 1);
        }
        List<Integer> vectorDependencies = getVectorDependencies();
        return _belowBroadcastAbstraction.broadcast(Utils.addVectorClock(uuid, vectorDependencies), message); // need to send current vector clock, with non affectedProcesses with 0s
    }

    @Override
    public boolean deliver(String rawMessage) throws IOException {
        String messageHeader = Utils.getMessageHeader(rawMessage);
        String UUID = Utils.getUUID(messageHeader);
        int processId = Integer.parseInt(Utils.getNonce(UUID));
        List<Integer> messageVectorClock = Utils.getVectorClock(UUID);
        System.out.println(messageVectorClock);

        List<String> messagesToDeliver = new ArrayList<>();

        synchronized (_currentVectorClock){
            if (canDeliver(messageVectorClock)){
                _currentVectorClock.set(processId, _currentVectorClock.get(processId) + 1);
                messagesToDeliver.add(rawMessage);


                return true;
            }
            else {
                _pendingMessages.put(UUID, new MessageWithVectorClock(messageVectorClock, rawMessage));
            }
        }

        return false;
    }

    public List<Integer> getVectorDependencies(){
        List<Integer> vectorDependencies = new ArrayList<>(Collections.nCopies(_currentVectorClock.size(), 0));
        for (int processId : _affectedProcesses){
            vectorDependencies.set(processId, _currentVectorClock.get(processId));
        }
        return vectorDependencies;
    }

    public boolean canDeliver(List<Integer> messageVectorClock){
        for (int i = 0; i < messageVectorClock.size(); i++){
            if (messageVectorClock.get(i) > _currentVectorClock.get(i)){
                return false;
            }
        }
        return true;
    }

    class MessageWithVectorClock {
        List<Integer> _vectorClock;
        String _message;

        public MessageWithVectorClock(List<Integer> vectorClock, String message){
            _vectorClock = vectorClock;
            _message = message;
        }

    }
}
