package ru.spbstu.mypipeline;

import org.jetbrains.annotations.NotNull;
import ru.spbstu.pipeline.Consumer;
import ru.spbstu.pipeline.Executor;
import ru.spbstu.pipeline.Producer;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.util.*;


public class MyExecutor implements Executor {
    enum Modes {Code, Decode;}
    enum ConfigParams{MODE}
    ArrayList<Consumer> consumers;
    private Producer.DataAccessor dataAccessor;
    Status status = Status.OK;
    String data, result;
    String needTypeName;
    private String mode;
    Logger log;

    public static final String[] dataTypes;

    static {
        dataTypes = new String[]{byte[].class.getCanonicalName(),
            char[].class.getCanonicalName(),
            String.class.getCanonicalName()};
    }

    public MyExecutor(String ConfigName, Logger log) {
        consumers = new ArrayList<>();
        this.log = log;
        Config c = new Config(ConfigName, new String[]{ConfigParams.MODE.toString()}, (log));
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
    public long loadDataFrom(Producer producer) {
            data = (String) dataAccessor.get();
        return dataAccessor.size();
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
            long check = c.loadDataFrom(this);

            if (c.status() != Status.OK || check == 0) {
                status = Status.ERROR;
                return;
            }
            c.run();
        }
    }

    @Override
    public void addProducer(@NotNull Producer producer) {
        if(!producer.outputDataTypes().contains(String.class.getCanonicalName()))
            status = Status.EXECUTOR_ERROR;
        else
            dataAccessor = producer.getAccessor(String.class.getCanonicalName());
    }

    @Override
    public void addProducers(@NotNull List<Producer> producers) { }

    @Override
    public void addConsumer(@NotNull Consumer consumer) {
        consumers.add(consumer);
    }

    @Override
    public void addConsumers(@NotNull List<Consumer> consumers) {
        this.consumers.addAll(consumers);
    }

    @NotNull
    @Override
    public Status status() {
        return status;
    }

    private final class DataAccessor implements Producer.DataAccessor {
        @Override
        public Object get() {
            if(needTypeName.equals(dataTypes[dataTypesEnum.BYTE.ordinal()]))
                return result.getBytes(StandardCharsets.UTF_16BE);

            if(needTypeName.equals(dataTypes[dataTypesEnum.CHAR.ordinal()]))
                return result.toCharArray();

            if(needTypeName.equals(dataTypes[dataTypesEnum.STRING.ordinal()]))
                return new String(result);

            status = Status.ERROR;
            return null;
        }

        @Override
        public long size() {
            if(needTypeName.equals(dataTypes[dataTypesEnum.STRING.ordinal()]))
                return result.length();

            return (result.getBytes(StandardCharsets.UTF_16BE)).length;
        }
    }

    @Override
    public Producer.@NotNull DataAccessor getAccessor(@NotNull String s) {
        needTypeName = s;
        return new MyExecutor.DataAccessor();
    }

    @Override
    public @NotNull Set<String> outputDataTypes() {
        return new HashSet<>(Arrays.asList(MyExecutor.dataTypes));
    }
}
