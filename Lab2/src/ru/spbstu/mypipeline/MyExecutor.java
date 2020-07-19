package ru.spbstu.mypipeline;

import ru.spbstu.pipeline.Consumer;
import ru.spbstu.pipeline.Executor;
import ru.spbstu.pipeline.Producer;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.logging.Logger;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Arrays;
import java.io.FileNotFoundException;


public class MyExecutor implements Executor {
    enum Modes {Code, Decode;}
    enum ConfigParams{MODE}

    ArrayList<Consumer> consumers;
    Status status = Status.OK;
    String data, result;
    private String mode;
    Logger log;

    public MyExecutor(String ConfigName, Logger log) {
        consumers = new ArrayList<>();
        this.log = log;
        Config c = new Config(ConfigName, new String[]{ConfigParams.MODE.toString()}, (Log)(log));
        c.readConfiguration();

        mode = (String)(c.valueOf(ConfigParams.MODE.toString())[0]);
    }

    private void AddToRleCode(StringBuilder sb, final int currentCharCount, final char currentChar){
        sb.append(currentChar);

        if (currentCharCount > 1) {
            sb.append(currentCharCount);
        }
    }

    private void getRLE() {
        final String emptyStr = "";

        if (data == null || data.equals(emptyStr)) {
            return;
        }

        char currentChar = data.charAt(0);
        int currentCharCount = 1;

        final int maxNumber = 9;

        StringBuilder sb = new StringBuilder();

        for (int i = 1; i < data.length(); i++) {
            char c = data.charAt(i);

            if (c != currentChar || currentCharCount == maxNumber) {
                AddToRleCode(sb, currentCharCount, currentChar);

                currentCharCount = 1;
                currentChar = c;
            } else {
                currentCharCount++;
            }
        }

        AddToRleCode(sb, currentCharCount, currentChar);

        result = sb.toString();
    }

    private void Decode() {
        final String emptyStr = "";

        if (data == null || data.equals(emptyStr)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        char CurrentChar = 0;

        for(int i = 0; i < data.length(); i++) {
            if(Character.isDigit(data.charAt(i))) {
                for(int j = 1; j < Character.getNumericValue(data.charAt(i)); j++) {
                    sb.append(CurrentChar);
                }
            } else {
                CurrentChar = data.charAt(i);
                sb.append(CurrentChar);
            }
        }

        result = sb.toString();
    }

    @Override
    public void loadDataFrom(Producer producer) {
            data = new String(producer.get(), StandardCharsets.UTF_16BE);
    }

    @Override
    public void run() {
        if (mode.equals(Modes.Code.toString())) {
            getRLE();
        } else if (mode.equals(Modes.Decode.toString())) {
            Decode();
        } else {
            status = Status.EXECUTOR_ERROR;
            return;
        }
        for (Consumer c: consumers) {
            if (c.status() != Status.OK) {
                status = Status.ERROR;
                return;
            }
            c.loadDataFrom(this);
            c.run();
        }
    }

    @Override
    public void addProducer(Producer producer) {}

    @Override
    public void addProducers(List<Producer> producers) {}

    @Override
    public byte[] get() {
        return result.getBytes(StandardCharsets.UTF_16BE);
    }

    @Override
    public void addConsumer(Consumer consumer) {
        consumers.add(consumer);
    }

    @Override
    public void addConsumers(List<Consumer> consumers) {
        this.consumers.addAll(consumers);
    }

    @Override
    public Status status() {
        return status;
    }
}
