package cs451.applications;

import java.io.IOException;
import java.net.InetAddress;

public abstract class Channel {

    Channel _aboveChannel;
    Channel _belowChannel;

    public Channel(){
        _aboveChannel = null;
        _belowChannel = null;
    }

    public void setAboveChannel(Channel aboveChannel){
        _aboveChannel = aboveChannel;
    }

    public void setBelowChannel(Channel belowChannel){
        _belowChannel = belowChannel;
    }

    /**
     *
     * @param message message to be sent
     * @param destIp ip of the destination address
     * @param destPort port of the destination address
     * @return truth value of operation
     */
    abstract public boolean send(String messageHeader, String message, int destId, InetAddress destIp, int destPort) throws IOException;

    /**
     * @param rawMessage message to be received
     * @return truth value of operation
     */
    abstract public boolean deliver(String rawMessage) throws IOException;

}