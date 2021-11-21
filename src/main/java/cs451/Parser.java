package cs451;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Parser {

    private String[] args;
    private long pid;
    private IdParser idParser;
    private HostsParser hostsParser;
    private OutputParser outputParser;
    private ConfigParser configParser;

    public Parser(String[] args) {
        this.args = args;
    }

    public void parse() {
        pid = ProcessHandle.current().pid();

        idParser = new IdParser();
        hostsParser = new HostsParser();
        outputParser = new OutputParser();
        configParser = new ConfigParser();

        int argsNum = args.length;
        if (argsNum != Constants.ARG_LIMIT_CONFIG) {
            help();
        }

        if (!idParser.populate(args[Constants.ID_KEY], args[Constants.ID_VALUE])) {
            help();
        }

        if (!hostsParser.populate(args[Constants.HOSTS_KEY], args[Constants.HOSTS_VALUE])) {
            help();
        }

        if (!hostsParser.inRange(idParser.getId())) {
            help();
        }

        if (!outputParser.populate(args[Constants.OUTPUT_KEY], args[Constants.OUTPUT_VALUE])) {
            help();
        }

        if (!configParser.populate(args[Constants.CONFIG_VALUE])) {
            help();
        }
    }

    private void help() {
        System.err.println("Usage: ./run.sh --id ID --hosts HOSTS --output OUTPUT CONFIG");
        System.exit(1);
    }

    public int myId() {
        return idParser.getId();
    }

    public int myPort() { return getHostById(myId()).getPort();}

    public Host getHostById(int id) {return hostsParser.getHosts().stream().filter(x -> x.getId() == id).findAny().get();}

    public List<Host> hosts() {
        return hostsParser.getHosts();
    }

    public String output() {
        return outputParser.getPath();
    }

    public String config() {
        return configParser.getPath();
    }

    public List<Integer> getDependencies(int id) { return configParser.getDependencies(id);}

    public int getMessageNumber() {
        File configFile = new File(config());
        int messageNumber = 0;
        try {
            Scanner reader = new Scanner(configFile);
            String data = reader.nextLine().split(" ")[0];
            messageNumber = Integer.parseInt(data);
            reader.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return messageNumber;
    }

    public int getDestination() {
        File configFile = new File(config());
        int destination = -1;
        try {
            Scanner reader = new Scanner(configFile);
            String data = reader.nextLine().split(" ")[1];
            destination = Integer.parseInt(data);
            reader.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return destination;
    }

}
