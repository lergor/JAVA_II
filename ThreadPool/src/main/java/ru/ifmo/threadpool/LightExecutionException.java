package ru.ifmo.threadpool;

public class LightExecutionException extends Exception {
    LightExecutionException(Throwable throwable) {
        super(throwable);
    }
}
