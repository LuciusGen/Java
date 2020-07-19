package ru.spbstu.mypipeline;

import ru.spbstu.mypipeline.Config;
import ru.spbstu.mypipeline.Log;
import ru.spbstu.pipeline.Producer;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.logging.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Writer implements ru.spbstu.pipeline.Writer {
    enum ConfigParam {OUTPUT_FILE}

    private FileOutputStream fOut;
    Status status = Status.OK;
    byte[] data;
    Log log;

    public Writer(String ConfigName, Logger log) {
        Config c = new Config(ConfigName, new String[]{ConfigParam.OUTPUT_FILE.toString()}, (Log) log);
        c.readConfiguration();
        this.log = (Log)log;

        if(this.log.Status()) {
            try {
                fOut = new FileOutputStream((String) c.valueOf(ConfigParam.OUTPUT_FILE.toString())[0]);
            } catch (IOException e) {
                this.status = Status.WRITER_ERROR;
                log.log("Error with Writer file");
            }
        }
    }

    @java.lang.Override
    public void loadDataFrom(Producer producer) {
        data = producer.get();
    }

    public void end() {
        try {
            fOut.close();
        } catch (IOException e) {
            this.status = Status.WRITER_ERROR;
            log.log("Error with close");
        }
    }

    @java.lang.Override
    public void run() {
        try {
            fOut.write(data);
        } catch (IOException e) {
            this.status = Status.WRITER_ERROR;
            log.log("Error with write");
        }
    }

    @java.lang.Override
    public Status status() {
        return status;
    }

    @java.lang.Override
    public void addProducer(Producer producer) {}

    @java.lang.Override
    public void addProducers(List<Producer> producers) {}
}