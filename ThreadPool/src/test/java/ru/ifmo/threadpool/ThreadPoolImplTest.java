package ru.ifmo.threadpool;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class ThreadPoolImplTest {

    private final int threadNumber = 4;

    @Test
    public void TestCorrectThreadsNumber() {
        int otherThreads = Thread.activeCount();
        ThreadPool threadPool = new ThreadPoolImpl(threadNumber);
        assertThat(Thread.activeCount() - otherThreads).isEqualTo(threadNumber);
    }

    @Test
    public void testManyIndependentTasks() throws LightExecutionException {
        ThreadPool threadPool = new ThreadPoolImpl(threadNumber);
        List<LightFuture<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            final int j = i;
            tasks.add(threadPool.submit(() -> j * j));
        }
        for (int i = 0; i < 1000; i++) {
            assertThat(tasks.get(i).get()).isEqualTo(i * i);
        }
    }

    @Test
    public void testDependentTasksCorrectOrder() throws LightExecutionException {
        ThreadPool threadPool = new ThreadPoolImpl(threadNumber);
        List<LightFuture<Integer>> tasks = new ArrayList<>();
        tasks.add(threadPool.submit(() -> 1));
        for (int i = 1; i < 10; i++) {
            tasks.add(tasks.get(i - 1).thenApply(k -> k * 2));
        }
        for (int i = 0; i < 10; i++) {
            assertThat(tasks.get(i).get()).isEqualTo((int) Math.pow(2, i));
        }
    }

    @Test
    public void testDependentTasksReverseOrder() throws LightExecutionException {
        ThreadPool threadPool = new ThreadPoolImpl(threadNumber);
        List<LightFuture<Integer>> tasks = new ArrayList<>();
        tasks.add(threadPool.submit(() -> 1));
        for (int i = 1; i < 10; i++) {
            tasks.add(tasks.get(i - 1).thenApply(k -> k * 2));
        }
        for (int i = 9; i >= 0; i--) {
            assertThat(tasks.get(i).get()).isEqualTo((int) Math.pow(2, i));
        }
    }

    @Test(expected = LightExecutionException.class)
    public void testLightExecutionException() throws LightExecutionException {
        ThreadPool threadPool = new ThreadPoolImpl(threadNumber);
        LightFuture<Integer> task = threadPool.submit(() -> {
            throw new RuntimeException("kek!");
        });
        task.get();
    }

    @Test
    public void TestShutdown() {
        int otherThreads = Thread.activeCount();
        ThreadPool threadPool = new ThreadPoolImpl(threadNumber);
        assertThat(Thread.activeCount() - otherThreads).isEqualTo(threadNumber);
        threadPool.shutdown();
        assertThat(Thread.activeCount()).isEqualTo(otherThreads);
    }

}
