package cs451.applications;

import cs451.Host;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BestEffortBroadcast extends BroadcastAbstraction{
    PerfectLinks _perfectLinksChannel;
    List<Host> _destinationHosts;
    String _processId;

    public BestEffortBroadcast(List<Host> destinationHosts, String processId){
        super();
        _destinationHosts = destinationHosts;
        _processId = processId;
    }

    public void setPerfectLinks(PerfectLinks perfectLinksChannel){
        _perfectLinksChannel = perfectLinksChannel;
    }

    @Override
    public boolean broadcast(String uuid, String message) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (Host host : _destinationHosts){
            String messageHeader = Utils.createMessageHeader(_processId, uuid);
            MessageSender obj = new MessageSender(_perfectLinksChannel, messageHeader, message, host.getId(), InetAddress.getByName(host.getIp()), host.getPort());
            executorService.execute(obj);
        }
        executorService.shutdown();
        return true;
    }

    @Override
    public boolean deliver(String rawMessage) throws IOException {
        if (_aboveBroadcastAbstraction != null) {
            return _aboveBroadcastAbstraction.deliver(rawMessage);
        }
        return true;
    }
}
