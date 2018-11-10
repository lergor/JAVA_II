package ru.ifmo.threadpool;

import java.util.function.Function;

public interface LightFuture<X> {

    boolean isReady();

    X get() throws LightExecutionException;

    <Y> LightFuture<Y> thenApply(Function<? super X, ? extends Y> function);
}
