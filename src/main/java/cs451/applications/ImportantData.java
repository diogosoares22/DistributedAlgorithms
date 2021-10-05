package cs451.applications;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ImportantData {

    public static final ConcurrentHashMap<String, String> _messageDB = new ConcurrentHashMap<String, String>();
    public static final ConcurrentHashMap<String, String> _ackDB = new ConcurrentHashMap<String, String>();
    public static final ConcurrentLinkedQueue<String> _logs = new ConcurrentLinkedQueue<String>();

}
