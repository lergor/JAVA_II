package ru.ifmo.threadpool;

import java.util.function.Supplier;

public class ThreadPoolImpl implements ThreadPool {

    ThreadPoolImpl(int threadsNumber) {

    }

    @Override
    public <R> LightFuture<R> submit(Supplier<R> supplier) {
        return null;
    }

    @Override
    public void shutdown() {

    }
}
