package cs451;

import cs451.applications.*;
import jdk.jshell.execution.Util;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static void handleSignal(String output){
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");

        StringBuilder logs = new StringBuilder();
        for (String log : ImportantData._logs) {
            logs.append(log).append("\n");
        }
        try {
            File file = new File(output);
            FileWriter fileWriter = new FileWriter(file);
            if (logs.length() > 0){
                fileWriter.write(logs.substring(0,logs.length() - 1));
            }
            else {
                fileWriter.write("");
            }
            fileWriter.flush();
            fileWriter.close();

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void initSignalHandlers(String output) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal(output);
            }
        });
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers(parser.output());

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");

        FairLossLinks fairLossLink = new FairLossLinks();

        StubbornLinks stubbornLink = new StubbornLinks(parser);

        PerfectLinks perfectLink = new PerfectLinks(ImportantData._messageDB);

        BestEffortBroadcast bestEffortBroadcast = new BestEffortBroadcast(parser.hosts(), String.valueOf(parser.myId()));

        UniformReliableBroadcast uniformReliableBroadcast = new UniformReliableBroadcast(parser.hosts().size(), String.valueOf(parser.myId()));

        FIFOBroadcast fifoBroadcast = new FIFOBroadcast(parser.hosts().size(), ImportantData._logs);

        LocalizedCausalBroadcast localizedCausalBroadcast = new LocalizedCausalBroadcast(parser.myId(), parser.getDependencies(parser.myId()), parser.hosts().size());

        fairLossLink.setAboveChannel(stubbornLink);

        stubbornLink.setBelowChannel(fairLossLink);

        stubbornLink.setAboveChannel(perfectLink);

        perfectLink.setBelowChannel(stubbornLink);

        perfectLink.setBestEffortBroadcast(bestEffortBroadcast);

        bestEffortBroadcast.setPerfectLinks(perfectLink);

        bestEffortBroadcast.setAboveBroadcastAbstraction(uniformReliableBroadcast);

        uniformReliableBroadcast.setBelowBroadcastAbstraction(bestEffortBroadcast);

        /* Fifo broadcast */

        uniformReliableBroadcast.setAboveBroadcastAbstraction(fifoBroadcast);

        fifoBroadcast.setBelowBroadcastAbstraction(uniformReliableBroadcast);

        /* Localized reliable broadcast */

        // uniformReliableBroadcast.setAboveBroadcastAbstraction(localizedCausalBroadcast);

        // localizedCausalBroadcast.setBelowBroadcastAbstraction(uniformReliableBroadcast);

        ExecutorService executor_receiver = Executors.newFixedThreadPool(5);

        MessageReceiver receiver = new MessageReceiver(parser.myPort(), fairLossLink, executor_receiver);

        int noMessages = parser.getMessageNumber();

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        receiver.start();

        System.out.println("Current timestamp -> " + timestamp.toString() + "\n");

        System.out.println("Broadcasting and delivering messages...\n");


        for (int i = 1; i < noMessages + 1; i++) {
            String uuid = Utils.createUUID(String.valueOf(i), Integer.toString(parser.myId()));

            /* Fifo broadcast */
            fifoBroadcast.broadcast(uuid, Integer.toString(i));

            /* Localized reliable broadcast */
            // localizedCausalBroadcast.broadcast(uuid, Integer.toString(i));
        }


        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
