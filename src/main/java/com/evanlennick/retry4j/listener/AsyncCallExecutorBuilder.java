package com.evanlennick.retry4j.listener;

import com.evanlennick.retry4j.AsyncCallExecutor;
import com.evanlennick.retry4j.RetryConfig;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class AsyncCallExecutorBuilder<T> {

    private RetryConfig config;
    private ArrayList<RetryListener> retryListeners = new ArrayList<>();
    private Callable<T> callable;

    public static <T> AsyncCallExecutorBuilder newAsyncCall(Callable<T> callable) {
        return new AsyncCallExecutorBuilder(callable);
    }

    private AsyncCallExecutorBuilder(Callable<T> callable) {
        if (callable == null) throw new NullPointerException("Callable cannot be null.");
        this.callable = callable;
    }

    public AsyncCallExecutorBuilder withConfig(RetryConfig config) {
        if (config == null) throw new NullPointerException("Retry configuration cannot be null.");
        this.config = config;
        return this;
    }

    public AsyncCallExecutorBuilder afterFailedTry(AfterFailedTryListener afterFailedTryListener) {
        return addRetryListener(afterFailedTryListener);
    }

    public AsyncCallExecutorBuilder beforeNextTry(BeforeNextTryListener beforeNextTryListener) {
        return addRetryListener(beforeNextTryListener);
    }

    public AsyncCallExecutorBuilder onFailure(OnFailureListener onFailureListener) {
        return addRetryListener(onFailureListener);
    }

    public AsyncCallExecutorBuilder onSuccess(OnSuccessListener onSuccessListener) {
        return addRetryListener(onSuccessListener);
    }

    private AsyncCallExecutorBuilder addRetryListener(RetryListener retryListener) {
        if (retryListener == null) throw new NullPointerException("Retry listener configuration cannot be null.");
        this.retryListeners.add(retryListener);
        return this;
    }

    public void execute() {
        AsyncCallExecutor asyncCallExecutor = new AsyncCallExecutor();
        asyncCallExecutor.execute(this.callable, this.config, this.retryListeners);
    }

}
