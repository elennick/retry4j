package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.listener.RetryListener;
import lombok.Builder;

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

    private final RetryConfig config;

    private final ExecutorService executorService;

    private final RetryListener afterFailedTryListener;

    private final RetryListener beforeNextTryListener;

    private final RetryListener onFailureListener;

    private final RetryListener onSuccessListener;

    private final RetryListener onCompletionListener;

    private static final int DEFAULT_NUMBER_OF_THREADS_IN_POOL = 5;

    @Builder
    public AsyncCallExecutor(RetryConfig withConfig,
                             RetryListener afterFailedTry,
                             RetryListener beforeNextTry,
                             RetryListener onFailure,
                             RetryListener onSuccess,
                             RetryListener onCompletion,
                             ExecutorService executorService) {
        this.config = withConfig;
        this.afterFailedTryListener = afterFailedTry;
        this.beforeNextTryListener = beforeNextTry;
        this.onFailureListener = onFailure;
        this.onSuccessListener = onSuccess;
        this.onCompletionListener = onCompletion;

        if (executorService == null) {
            this.executorService = Executors.newFixedThreadPool(DEFAULT_NUMBER_OF_THREADS_IN_POOL);
        } else {
            this.executorService = executorService;
        }
    }

    @Override
    public CompletableFuture<Status<T>> execute(Callable<T> callable) {
        return execute(callable, null);
    }

    @Override
    public CompletableFuture<Status<T>> execute(Callable<T> callable, String callName) {
        CallExecutor<T> synchronousCallExecutor = CallExecutor.<T>builder()
                .withConfig(config)
                .afterFailedTry(afterFailedTryListener)
                .beforeNextTry(beforeNextTryListener)
                .onSuccess(onSuccessListener)
                .onFailure(onFailureListener)
                .onCompletion(onCompletionListener)
                .build();

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

    public ExecutorService getThreadExecutorService() {
        return executorService;
    }

}
