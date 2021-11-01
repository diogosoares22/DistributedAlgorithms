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

        FairLossLinks fairLossLink = new FairLossLinks(ImportantData._logs);

        StubbornLinks stubbornLink = new StubbornLinks(ImportantData._logs, parser);

        PerfectLinks perfectLink = new PerfectLinks(ImportantData._logs, ImportantData._messageDB);

        fairLossLink.setAboveChannel(stubbornLink);

        stubbornLink.setBelowChannel(fairLossLink);

        stubbornLink.setAboveChannel(perfectLink);

        perfectLink.setBelowChannel(stubbornLink);

        ExecutorService executor_receiver = Executors.newFixedThreadPool(15);

        MessageReceiver receiver = new MessageReceiver(parser.myPort(), fairLossLink, executor_receiver);

        int noMessages = parser.getMessageNumber();

        int destinationId = parser.getDestination();

        Host destination = parser.getHostById(destinationId);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        System.out.println("Current timestamp -> " + timestamp.toString() + "\n");

        System.out.println("Broadcasting and delivering messages...\n");

        receiver.start();

        if (parser.myId() != destinationId) {
            ExecutorService executor = Executors.newFixedThreadPool(15);
            for (int i = 1; i < noMessages + 1; i++) {
                String uuid = Utils.createUUID(Integer.toString(parser.myId()), String.valueOf(i));
                MessageSender obj = new MessageSender(perfectLink, uuid, String.valueOf(i) , InetAddress.getByName(destination.getIp()), destination.getPort());
                executor.execute(obj);
            }
        }

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
