package com.evanlennick.retry4j;

import com.evanlennick.retry4j.listener.RetryListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncCallExecutor {

    private ExecutorService executorService;

    public AsyncCallExecutor() {
        executorService = Executors.newFixedThreadPool(10);
    }

    public AsyncCallExecutor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void execute(Callable<?> callable, RetryConfig config) {
        this.execute(callable, config, Arrays.asList());
    }

    public void execute(Callable<?> callable, RetryConfig config, List<RetryListener> listeners) {
        SyncCallExecutor retry4JCallExecutor = new SyncCallExecutor(config);
        listeners.stream().forEach(retry4JCallExecutor::registerRetryListener);

        Runnable runnable = () -> retry4JCallExecutor.execute(callable);
        executorService.execute(runnable);
    }
}
