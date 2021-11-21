package cs451;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ConfigParser {

    private String path;

    private HashMap<Integer, List<Integer>> dependencies = new HashMap<>();

    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();
        try {
            Scanner reader = new Scanner(file);
            reader.nextLine();
            while (reader.hasNext()){
                String[] data = reader.nextLine().split(" ");
                List<Integer> dependentElems = new ArrayList<>();
                for (String el : data){
                    dependentElems.add(Integer.parseInt(el));
                }
                dependencies.put(Integer.parseInt(data[0]), dependentElems);
            }
        } catch (FileNotFoundException e) {}
        return true;
    }

    public String getPath() {
        return path;
    }

    public List<Integer> getDependencies(int id) { return dependencies.get(id);}

}
