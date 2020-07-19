package ru.spbstu.mypipeline;

import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class Main {

    public static void main(String[] args) {
        final String logFilename = "error.log";

        if (args.length != 1) {
            System.err.println("Not correct args in command line");
            return;
        }

        Log logger = new Log();
        try (PrintWriter log = new PrintWriter(logFilename)) {
            Manager manager = new Manager(args[0], logger);

            if(logger.Status())
                manager.run();

            if(!logger.Status()) {
                System.err.println("error");
                log.println(logger.error);
            }
        }
        catch (FileNotFoundException e) {
            System.err.println("log file error");
        }
    }
}
