package ru.ifmo.threadpool;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThreadPoolImpl implements ThreadPool {

    private final List<Thread> threads = new ArrayList<>();

    private volatile boolean isWorking;

    private final Queue<LightFutureImpl> taskQueue = new LinkedList<>();

    public ThreadPoolImpl(int threadsNumber) {
        for (int i = 0; i < threadsNumber; i++) {
            Thread thread = new Thread(new ThreadPoolWorker());
            thread.start();
            threads.add(thread);
        }
        isWorking = true;
    }

    @Override
    public <R> LightFuture<R> submit(Supplier<R> supplier) {
        if (!isWorking) {
            return null;
        }
        LightFutureImpl<R> task = new LightFutureImpl<>(supplier);
        synchronized (taskQueue) {
            pushTaskToQueue(task);
        }
        return task;
    }

    @Override
    public void shutdown() {
        threads.forEach(Thread::interrupt);
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
        isWorking = false;
    }

    private void pushTaskToQueue(LightFutureImpl task) {
        synchronized (taskQueue) {
            taskQueue.add(task);
            taskQueue.notify();
        }
    }

    private class LightFutureImpl<R> implements LightFuture<R>, Runnable {

        private final Supplier<R> task;
        private volatile boolean isReady = false;
        private volatile R result = null;
        private volatile LightExecutionException exception = null;
        private List<LightFutureImpl> waitingTasks = new ArrayList<>();

        private LightFutureImpl(Supplier<R> task) {
            this.task = task;
        }

        @Override
        public boolean isReady() {
            return isReady;
        }

        @Override
        public R get() throws LightExecutionException {
            while (!isReady()) {
                Thread.yield();
            }
            if (exception != null) {
                throw exception;
            }
            return result;
        }

        @Override
        public <Y> LightFuture<Y> thenApply(Function<? super R, ? extends Y> function) {
            Supplier<Y> supplier = () -> {
                try {
                    return function.apply(this.get());
                } catch (LightExecutionException e) {
                    throw new RuntimeException(e.getMessage(), e.getCause());
                }
            };
            LightFutureImpl<Y> nextTask = new LightFutureImpl<>(supplier);
            synchronized (this) {
                if (!isReady) {
                    waitingTasks.add(nextTask);
                } else {
                    ThreadPoolImpl.this.pushTaskToQueue(nextTask);
                }
            }
            return nextTask;
        }

        private synchronized void submitWaitingTasks() {
            waitingTasks.forEach(ThreadPoolImpl.this::pushTaskToQueue);
            waitingTasks.clear();
        }

        @Override
        public void run() {
            try {
                result = task.get();
            } catch (Exception e) {
                exception = new LightExecutionException(e);
            }
            isReady = true;
            submitWaitingTasks();
        }
    }

    private class ThreadPoolWorker implements Runnable {

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    LightFutureImpl task;
                    synchronized (taskQueue) {
                        if (taskQueue.isEmpty()) {
                            taskQueue.wait();
                        }
                        task = taskQueue.poll();
                    }
                    if (task != null) {
                        task.run();
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }
    }
}
