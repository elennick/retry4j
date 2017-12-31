package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.evanlennick.retry4j.listener.RetryListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation that kicks off each retry request in its own separate thread that does not block the thread the
 * execution is called from.
 *
 * @param <T> The type that is returned by the Callable (eg: Boolean, Void, Object, etc)
 */
public class ThreadedCallExecutor<T> implements RetryExecutor<T> {

    private RetryConfig config;

    private ExecutorService executorService;

    private List<RetryListener> listeners;

    private static final int DEFAULT_NUMBER_OF_THREADS_IN_POOL = 10;

    public ThreadedCallExecutor(RetryConfig config) {
        this(config, Executors.newFixedThreadPool(DEFAULT_NUMBER_OF_THREADS_IN_POOL));
    }

    public ThreadedCallExecutor(RetryConfig config, ExecutorService executorService) {
        this.config = config;
        this.executorService = executorService;
        this.listeners = new ArrayList<>();
    }

    @Override
    public Void execute(Callable<T> callable) throws RetriesExhaustedException, UnexpectedException {
        CallExecutor<T> synchronousCallExecutor = new CallExecutor<>(config);
        synchronousCallExecutor.registerRetryListeners(listeners);

        executorService.submit(() -> {
            synchronousCallExecutor.execute(callable);
        });

        return null;
    }

    @Override
    public void registerRetryListener(RetryListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void registerRetryListeners(List<RetryListener> listeners) {
        this.listeners.addAll(listeners);
    }

    @Override
    public void setConfig(RetryConfig config) {
        this.config = config;
    }

    public void shutdownExecutorService() {
        executorService.shutdown();
    }

    public void shutdownExecutorServiceNow() {
        executorService.shutdownNow();
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

}
