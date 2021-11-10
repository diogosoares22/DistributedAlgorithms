package cs451.applications;

import java.io.IOException;
import java.net.InetAddress;

public abstract class BroadcastAbstraction {
    BroadcastAbstraction _aboveBroadcastAbstraction;
    BroadcastAbstraction _belowBroadcastAbstraction;

    public BroadcastAbstraction(){
        _aboveBroadcastAbstraction = null;
        _belowBroadcastAbstraction = null;
    }

    public void setAboveBroadcastAbstraction(BroadcastAbstraction aboveBroadcastAbstraction){
        _aboveBroadcastAbstraction = aboveBroadcastAbstraction;
    }

    public void setBelowBroadcastAbstraction(BroadcastAbstraction belowBroadcastAbstraction){
        _belowBroadcastAbstraction = belowBroadcastAbstraction;
    }
    /**
     * @param uuid unique identifier of message
     * @param message message to be sent
     * @return truth value of operation
     */
    abstract public boolean broadcast(String uuid, String message) throws IOException;

    /**
     * @param rawMessage message to be received
     * @return truth value of operation
     */
    abstract public boolean deliver(String rawMessage) throws IOException;


}
