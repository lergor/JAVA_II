package ru.ifmo.threadpool;

import java.util.function.Function;

public interface LightFuture<R> {

    boolean isReady();

    R get() throws LightExecutionException;

    <Y> LightFuture<Y> thenApply(Function<? super R, ? extends Y> function);
}
