package ru.spbstu.mypipeline;

import org.jetbrains.annotations.NotNull;
import ru.spbstu.pipeline.Consumer;
import ru.spbstu.pipeline.Producer;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.logging.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Reader implements  ru.spbstu.pipeline.Reader {
    enum ConfigParam { INPUT_FILE, BUFF_SIZE;}

    ArrayList<Consumer> consumers = new ArrayList<>();
    private FileInputStream fin;
    private int bufferSize;
    byte [] data;
    String needTypeName;
    Log log;

    Status status = Status.OK;

    public Reader(String ConfigName, Logger log) {
        Config c = new Config(ConfigName, new String[]{ConfigParam.INPUT_FILE.toString(), ConfigParam.BUFF_SIZE.toString()}, log);
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
                if(sizeOfRead < bufferSize)
                    data = Arrays.copyOf(data, sizeOfRead);

                long check = c.loadDataFrom(this);

                if (c.status() != Status.OK || check == 0) {
                    status = Status.EXECUTOR_ERROR;
                    return;
                }

                c.run();
                status = c.status();
            }
        }
    }

    private final class DataAccessor implements Producer.DataAccessor{
        @Override
        public Object get() {
            if(needTypeName.equals(MyExecutor.dataTypes[dataTypesEnum.BYTE.ordinal()]))
                return data.clone();

            if(needTypeName.equals(MyExecutor.dataTypes[dataTypesEnum.CHAR.ordinal()]))
                return new String(data, StandardCharsets.UTF_16BE).toCharArray();

            if(needTypeName.equals(MyExecutor.dataTypes[dataTypesEnum.STRING.ordinal()]))
                return new String(data, StandardCharsets.UTF_16BE);

            status = Status.ERROR;
            return null;
        }

        @Override
        public long size() {
            if(needTypeName.equals(MyExecutor.dataTypes[dataTypesEnum.STRING.ordinal()]))
                return new String(data, StandardCharsets.UTF_16BE).length();

            return data.length;
        }
    }

    @Override
    public @NotNull DataAccessor getAccessor(@NotNull String s) {
        needTypeName = s;
        return new Reader.DataAccessor();
    }

    @java.lang.Override
    public void addConsumer(@NotNull Consumer consumer) {
        consumers.add(consumer);
    }

    @java.lang.Override
    public void addConsumers(@NotNull List<Consumer> consumers) {
        this.consumers.addAll(consumers);
    }

    @Override
    public @NotNull Set<String> outputDataTypes() {
        return new HashSet<>(Arrays.asList(MyExecutor.dataTypes));
    }
}