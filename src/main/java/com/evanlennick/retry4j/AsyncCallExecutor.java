package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.listener.RetryListener;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation that kicks off each retry request in its own separate thread that does not block the thread the
 * execution is called from.
 *
 * @param <T> The type that is returned by the Callable (eg: Boolean, Void, Object, etc)
 */
public class AsyncCallExecutor<T> implements RetryExecutor<T, CompletableFuture<Status<T>>> {

    private RetryConfig config;

    private ExecutorService executorService;

    private RetryListener afterFailedTryListener;

    private RetryListener beforeNextTryListener;

    private RetryListener onFailureListener;

    private RetryListener onSuccessListener;

    private RetryListener onCompletionListener;

    private static final int DEFAULT_NUMBER_OF_THREADS_IN_POOL = 5;

    public AsyncCallExecutor(RetryConfig config) {
        this(config, Executors.newFixedThreadPool(DEFAULT_NUMBER_OF_THREADS_IN_POOL));
    }

    public AsyncCallExecutor(RetryConfig config, ExecutorService executorService) {
        this.config = config;
        this.executorService = executorService;
    }

    @Override
    public CompletableFuture<Status<T>> execute(Callable<T> callable) {
        return execute(callable, null);
    }

    @Override
    public CompletableFuture<Status<T>> execute(Callable<T> callable, String callName) {
        CallExecutor<T> synchronousCallExecutor = new CallExecutor<>(config);

        synchronousCallExecutor.afterFailedTry(afterFailedTryListener);
        synchronousCallExecutor.beforeNextTry(beforeNextTryListener);
        synchronousCallExecutor.onSuccess(onSuccessListener);
        synchronousCallExecutor.onFailure(onFailureListener);
        synchronousCallExecutor.onCompletion(onCompletionListener);

        CompletableFuture<Status<T>> completableFuture = new CompletableFuture<>();

        executorService.submit(() -> {
            try {
                Status<T> status = synchronousCallExecutor.execute(callable, callName);
                completableFuture.complete(status);
            } catch (Throwable t) {
                completableFuture.completeExceptionally(t);
            }
        });

        return completableFuture;
    }

    public AsyncCallExecutor<T> afterFailedTry(RetryListener listener) {
        this.afterFailedTryListener = listener;
        return this;
    }

    public AsyncCallExecutor<T> beforeNextTry(RetryListener listener) {
        this.beforeNextTryListener = listener;
        return this;
    }

    public AsyncCallExecutor<T> onCompletion(RetryListener listener) {
        this.onCompletionListener = listener;
        return this;
    }

    public AsyncCallExecutor<T> onSuccess(RetryListener listener) {
        this.onSuccessListener = listener;
        return this;
    }

    public AsyncCallExecutor<T> onFailure(RetryListener listener) {
        this.onFailureListener = listener;
        return this;
    }

    @Override
    public void setConfig(RetryConfig config) {
        this.config = config;
    }

    public ExecutorService getThreadExecutorService() {
        return executorService;
    }

}
