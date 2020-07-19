package ru.spbstu.mypipeline;


import org.jetbrains.annotations.NotNull;
import ru.spbstu.pipeline.logging.Logger;

import java.util.logging.Level;

public class Log implements Logger {
    public String error = null;

    public boolean Status(){
        return error == null;
    }

    @Override
    public void log(@NotNull String s) {
        error = s;
    }

    @Override
    public void log(@NotNull String s, @NotNull Throwable throwable) {

    }

    @Override
    public void log(@NotNull Level level, @NotNull String s) {

    }

    @Override
    public void log(@NotNull Level level, @NotNull String s, @NotNull Throwable throwable) {

    }
}