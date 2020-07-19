package ru.spbstu.mypipeline;

import ru.spbstu.pipeline.Consumer;
import ru.spbstu.pipeline.Producer;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.logging.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Reader implements  ru.spbstu.pipeline.Reader {
    enum ConfigParam { INPUT_FILE, BUFF_SIZE;}

    ArrayList<Consumer> consumers = new ArrayList<>();
    private FileInputStream fin;
    private int bufferSize;
    byte [] data;
    Log log;

    Status status = Status.OK;

    public Reader(String ConfigName, Logger log) {
        Config c = new Config(ConfigName, new String[]{ConfigParam.INPUT_FILE.toString(), ConfigParam.BUFF_SIZE.toString()}, (Log) log);
        c.readConfiguration();
        this.log = (Log) log;

        if(this.log.Status()) {
            try {
                fin = new FileInputStream((String) c.valueOf(ConfigParam.INPUT_FILE.toString())[0]);
            } catch (IOException e) {
                this.status = Status.READER_ERROR;
                log.log("Error with Reader file");
            }

            bufferSize = Integer.parseInt((String) c.valueOf(ConfigParam.BUFF_SIZE.toString())[0]);
            data = new byte[bufferSize];
        }
    }

    @Override
    public void run() {
        int sizeOfRead = 0;

        while (true) {
            try {
                if (!((sizeOfRead = fin.read(data)) > 0)) break;
            } catch (IOException e) {
                log.log("Read error");
                status = Status.READER_ERROR;
            }

            for (Consumer c: consumers) {
                if (c.status() != Status.OK) {
                    status = Status.EXECUTOR_ERROR;
                    return;
                }
                if(sizeOfRead < bufferSize)
                    data = Arrays.copyOf(data, sizeOfRead);

                c.loadDataFrom(this);
                c.run();
            }
        }
    }

    @java.lang.Override
    public byte[] get() {
        return data;
    }

    @java.lang.Override
    public void addConsumer(Consumer consumer) {
        consumers.add(consumer);
    }

    @java.lang.Override
    public void addConsumers(List<Consumer> consumers) {
        this.consumers.addAll(consumers);
    }
}