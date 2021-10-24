package cs451.applications;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/** code based on https://www.baeldung.com/udp-in-java **/

public class MessageReceiver extends Thread {

    private final DatagramSocket _socket;
    private final FairLossLinks _fairLossLink;
    private boolean _running = true;
    private final byte[] buf = new byte[256];
    private final ExecutorService _executorService;


    public MessageReceiver(int hostPort, FairLossLinks fairLossLink, ExecutorService executor) throws SocketException {
        _socket = new DatagramSocket(hostPort);
        _fairLossLink = fairLossLink;
        _executorService = executor;
    }

    @Override
    public void run(){
        while (_running){
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length);
            try {
                _socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            String received
                    = new String(packet.getData(), 0, packet.getLength());

            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        _fairLossLink.deliver(received);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            _executorService.execute(t1);

        }
        _socket.close();
    }
}
