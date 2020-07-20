package com.company;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;




public class Config {
    public enum ConfigParam { INPUT_FILE, OUTPUT_FILE, MODE; }
    public enum Modes{Code, Decode; }
    final private String Confname;

    public String[] names;

    Config(String filename) {
        this.Confname = filename;
    }

    private void IsConfigParam(String line, String[] names, Log log) {
        final int equalPosCheck = -1;
        final String equal = "=";
        final int equalSignPos = line.indexOf(equal);

        if (equalSignPos == equalPosCheck) {
            log.LogWrite("Error: Not correct syntax in \"" + Confname + "\"");
        }

        final ConfigParam Param;

        try {
            final String Paramis = line.substring(0, equalSignPos).trim();
            Param = ConfigParam.valueOf(Paramis);
        } catch (IllegalArgumentException e) {
            log.LogWrite("Error: Not correct key in \"" + Confname + "\"");
            return;
        } catch (NullPointerException e) {
            log.LogWrite("Error: Not correct syntax in \"" + Confname + "\"");
            return;
        }

        final String ParamValueis = line.substring(equalSignPos + 1).trim();

        names[Param.ordinal()] = ParamValueis;
    }

    public void readConfiguration(Log log){
        try (FileReader fin = new FileReader(Confname);
             BufferedReader in = new BufferedReader(fin)) {

            names = new String[ConfigParam.values().length];

            for(String line = in.readLine() ; line != null ; line = in.readLine()) {
                IsConfigParam(line, names, log);

                if (log.error != null)
                    return;
            }

            if (!names[ConfigParam.MODE.ordinal()].equals(Modes.Code.toString()) && !names[ConfigParam.MODE.ordinal()].equals(Modes.Decode.toString()))
                log.LogWrite("Error: Mode \"" + names[ConfigParam.MODE.ordinal()] + "\" not exist");

        } catch (FileNotFoundException e) {
            log.LogWrite("Error: File \"" + Confname + "\" not found");
        } catch (IOException e) {
            log.LogWrite("Error: With reading from \"" + Confname + "\"");
        }
    }
}
