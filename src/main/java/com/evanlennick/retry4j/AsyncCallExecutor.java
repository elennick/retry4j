package com.evanlennick.retry4j;

import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.evanlennick.retry4j.listener.RetryListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncCallExecutor<T> extends AbstractCallExecutor<Future<CallResults<T>>, T> {

    private ExecutorService executorService;
    private CallExecutor<CallResults<T>, T> callExecutor;

    public static <T> AsyncCallExecutor<T> newAsyncCallExecutor(CallExecutor<CallResults<T>, T> callExecutor) {
        return new AsyncCallExecutor<T>(callExecutor);
    }

    public static <T> AsyncCallExecutor<T> newAsyncCallExecutor(ExecutorService executorService, CallExecutor<CallResults<T>, T> callExecutor) {
        return new AsyncCallExecutor<T>(executorService, callExecutor);
    }

    private AsyncCallExecutor(CallExecutor<CallResults<T>, T> callExecutor) {
        this(Executors.newFixedThreadPool(10), callExecutor);
    }

    private AsyncCallExecutor(ExecutorService executorService, CallExecutor<CallResults<T>, T> callExecutor) {
        super();
        this.executorService = executorService;
        this.callExecutor = callExecutor;
    }

    @Override
    public Future<CallResults<T>> execute(Callable<T> callable) throws RetriesExhaustedException, UnexpectedException {
        return execute(callable, this.config);
    }

    @Override
    public Future<CallResults<T>> execute(Callable<T> callable, RetryConfig config) throws RetriesExhaustedException, UnexpectedException {
        return executorService.submit(new Callable<CallResults<T>>() {
            @Override
            public CallResults<T> call() throws Exception {
                return callExecutor.execute(callable, config);
            }
        });
    }
}
