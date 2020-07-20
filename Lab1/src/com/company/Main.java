package com.company;

import java.io.FileNotFoundException;
import java.io.PrintWriter;




public class Main {
    private static Log Solve(final String[] args) {
        if (args.length != 1) {
            System.err.println("Not correct arguments in command line");
            return null;
        }

        final String confFilename = args[0];
        Log log = new Log();

        Config Configuration = new Config(confFilename);
        Configuration.readConfiguration(log);

        if(log.error == null)
            RleProcess.Process(Configuration.names, log);

        return log;
    }

    public static void main(String[] args) {
        final String logFilename = "error.log";

        try (PrintWriter log = new PrintWriter(logFilename)) {
            Log logError = Solve(args);

            if(logError != null && logError.error != null) {
                    System.err.println("Error");
                    log.println(logError.error);
                }
        }
        catch (FileNotFoundException e) {
            System.err.println("log file error");
        }
    }
}
