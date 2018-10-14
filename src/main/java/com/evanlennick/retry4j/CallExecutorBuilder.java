package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.evanlennick.retry4j.listener.RetryListener;

import java.util.concurrent.ExecutorService;

public class CallExecutorBuilder<T> {

    public static final RetryConfig DEFAULT_RETRY_CONFIG = new RetryConfigBuilder().fixedBackoff5Tries10Sec().build();
    private RetryConfig retryConfig = DEFAULT_RETRY_CONFIG;
    private RetryListener afterFailedTryListener;
    private RetryListener beforeNextTryListener;
    private RetryListener onSuccessListener;
    private RetryListener onFailureListener;
    private RetryListener onCompletionListener;

    public CallExecutorBuilder() {
    }

    public CallExecutorBuilder config(RetryConfig retryConfig) {
        this.retryConfig = retryConfig;
        return this;
    }

    public CallExecutorBuilder onCompletionListener(RetryListener listener) {
        this.onCompletionListener = listener;
        return this;
    }

    public CallExecutorBuilder onSuccessListener(RetryListener listener) {
        this.onSuccessListener = listener;
        return this;
    }

    public CallExecutorBuilder onFailureListener(RetryListener listener) {
        this.onFailureListener = listener;
        return this;
    }

    public CallExecutorBuilder beforeNextTryListener(RetryListener listener) {
        this.beforeNextTryListener = listener;
        return this;
    }

    public CallExecutorBuilder afterFailedTryListener(RetryListener listener) {
        this.afterFailedTryListener = listener;
        return this;
    }

    public CallExecutor<T> build() {
        return new CallExecutor(retryConfig, afterFailedTryListener, beforeNextTryListener,
                onFailureListener, onSuccessListener, onCompletionListener);
    }

    public AsyncCallExecutor<T> buildAsync() {
        return new AsyncCallExecutor(retryConfig, null, afterFailedTryListener, beforeNextTryListener,
                onFailureListener, onSuccessListener, onCompletionListener);
    }

    public AsyncCallExecutor<T> buildAsync(ExecutorService executorService) {
        return new AsyncCallExecutor(retryConfig, executorService, afterFailedTryListener, beforeNextTryListener,
                onFailureListener, onSuccessListener, onCompletionListener);
    }
}
