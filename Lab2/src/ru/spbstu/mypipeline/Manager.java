package ru.spbstu.mypipeline;

import ru.spbstu.pipeline.Executor;
import ru.spbstu.pipeline.logging.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class Manager implements Runnable {
    private ArrayList<Executor> executors = new ArrayList<>();
    private Reader reader;
    private Writer writer;

    Log log;

    enum ReaderParams{READER, READER_CONF}
    enum WriterParams{WRITER, WRITER_CONF}
    enum ExecutorParams{EXECUTOR, EXECUTOR_CONF}

    private Object init(Logger log, String ClassName, String ConfName){
        try {
            Class cls = Class.forName(ClassName);
            return cls.getConstructor(String.class, Logger.class).newInstance(ConfName, log);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.log("Not Found " + ClassName + " class");
        }
        return null;
    }

    public Manager(String ConfigName, Log log) {
        Config c = new Config(ConfigName, new String[]{ReaderParams.READER.toString(), ReaderParams.READER_CONF.toString(),
                WriterParams.WRITER.toString(), WriterParams.WRITER_CONF.toString(),
                ExecutorParams.EXECUTOR.toString(), ExecutorParams.EXECUTOR_CONF.toString()}, log);

        c.readConfiguration();

        this.log = log;
        if (!log.Status())
            return;

        reader = (Reader)init((Logger)log, (String) c.valueOf(ReaderParams.READER.toString())[0], (String) c.valueOf(ReaderParams.READER_CONF.toString())[0]);
        writer = (Writer)init((Logger)log, (String) c.valueOf(WriterParams.WRITER.toString())[0], (String) c.valueOf(WriterParams.WRITER_CONF.toString())[0]);

        if(c.valueOf(ExecutorParams.EXECUTOR.toString()).length != c.valueOf(ExecutorParams.EXECUTOR_CONF.toString()).length)
            log.log("Check " + ConfigName + "quantity of executors not equal quantity of their config files");

        for (int i = 0; i < c.valueOf(ExecutorParams.EXECUTOR.toString()).length; i++) {
            String ExecutorName = (String) c.valueOf(ExecutorParams.EXECUTOR.toString())[i];
            String ExecutorConf = (String) c.valueOf(ExecutorParams.EXECUTOR_CONF.toString())[i];
            executors.add((Executor) init((Logger) log, ExecutorName, ExecutorConf));
        }
    }

    @Override
    public void run() {
        if (executors.isEmpty()) {
            reader.addConsumer(writer);
        } else {
            reader.addConsumer(executors.get(0));
            for (int i = 0; i < executors.size() - 1; ++i) {
                executors.get(i).addConsumer(executors.get(i + 1));
            }
            executors.get(executors.size() - 1).addConsumer(writer);
        }

        if(log.Status())
            reader.run();

        writer.end();
    }
}