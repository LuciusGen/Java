package ru.spbstu.mypipeline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Arrays;
import java.io.FileNotFoundException;

public class Config {
    final static private String equal = "=";
    private HashMap<String, ArrayList<String>> Params = new HashMap<>();
    final private String Confname;
    final private String[] configParams;
    Log log;

    Config(String filename, String[] configParams, Log log) {
        this.configParams = configParams;
        this.Confname = filename;
        this.log = log;
    }

    protected boolean CheckLine(String line){
        final int equalPosCheck = -1;
        final int equalSignPos = line.indexOf(equal);

        if (equalSignPos == equalPosCheck) {
            log.log("Error: Not correct syntax in \"" + Confname + "\"");
            return false;
        }

        final String Paramis = line.substring(0, equalSignPos).trim();

        if(!Arrays.asList(configParams).contains(Paramis)) {
            log.log("Error: Not correct key in \"" + Confname + "\"");
            return false;
        }

        final String ParamValueis = line.substring(equalSignPos + 1).trim();

        if(!Params.containsKey(Paramis))
            Params.put(Paramis, new ArrayList<>(Collections.singletonList(ParamValueis)));
        else {
            ArrayList<String> tmp = Params.get(Paramis);
            tmp.add(ParamValueis);

            Params.put(Paramis, tmp);
        }
        return true;
    }

    public void readConfiguration() {
        try (FileReader fin = new FileReader(Confname);
             BufferedReader in = new BufferedReader(fin)) {

            for (String line = in.readLine(); line != null; line = in.readLine()) {
                if (!CheckLine(line))
                    return;
            }
        } catch (FileNotFoundException e) {
            log.log("Error: File \"" + Confname + "\" not found");
        } catch (IOException e) {
            log.log("Error: With reading from \"" + Confname + "\"");
        }
    }

    public Object[] valueOf(String paramName) {
        return Params.get(paramName).toArray();
    }
}
