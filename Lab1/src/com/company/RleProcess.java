package com.company;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;




public class RleProcess {
    public static void Process(String[] names, Log log){
        try (FileWriter writer = new FileWriter(names[Config.ConfigParam.OUTPUT_FILE.ordinal()])) {

            try (FileReader fin = new FileReader(names[Config.ConfigParam.INPUT_FILE.ordinal()]); BufferedReader in = new BufferedReader(fin)) {

                for (String line = in.readLine(); line != null; line = in.readLine()) {
                    writer.write(Rle.RleSolve(line, names[Config.ConfigParam.MODE.ordinal()]) + '\n');
                }

            } catch (FileNotFoundException e) {
                log.LogWrite("Error: File \"" + names[Config.ConfigParam.INPUT_FILE.ordinal()] + "\" not found");
            } catch (IOException e) {
                log.LogWrite("Error: With reading from \"" + names[Config.ConfigParam.INPUT_FILE.ordinal()] + "\"");
            }

        } catch (IOException e) {
            log.LogWrite("Error: With write to \"" + names[Config.ConfigParam.OUTPUT_FILE.ordinal()] + "\"");
        }
    }
}
