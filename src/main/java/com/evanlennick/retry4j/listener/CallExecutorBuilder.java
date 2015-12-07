package com.evanlennick.retry4j.listener;

import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.CallResults;
import com.evanlennick.retry4j.RetryConfig;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class CallExecutorBuilder<T> {

    private RetryConfig config;
    private ArrayList<RetryListener> retryListeners = new ArrayList<>();
    private Callable<T> callable;

    public static <T> CallExecutorBuilder newSyncCall(Callable<T> callable) {
        return new CallExecutorBuilder(callable);
    }

    private CallExecutorBuilder(Callable<T> callable) {
        if (callable == null) throw new NullPointerException("Callable cannot be null.");
        this.callable = callable;
    }

    public CallExecutorBuilder withConfig(RetryConfig config) {
        if (config == null) throw new NullPointerException("Retry configuration cannot be null.");
        this.config = config;
        return this;
    }

    public CallExecutorBuilder afterFailedTry(AfterFailedTryListener afterFailedTryListener) {
        return addRetryListener(afterFailedTryListener);
    }

    public CallExecutorBuilder beforeNextTry(BeforeNextTryListener beforeNextTryListener) {
        return addRetryListener(beforeNextTryListener);
    }

    public CallExecutorBuilder onFailure(OnFailureListener onFailureListener) {
        return addRetryListener(onFailureListener);
    }

    public CallExecutorBuilder onSuccess(OnSuccessListener onSuccessListener) {
        return addRetryListener(onSuccessListener);
    }

    private CallExecutorBuilder addRetryListener(RetryListener retryListener) {
        if (retryListener == null) throw new NullPointerException("Retry listener configuration cannot be null.");
        this.retryListeners.add(retryListener);
        return this;
    }

    public CallResults<T> execute() {
        CallExecutor<T> callExecutor = new CallExecutor<>(config);
        retryListeners.stream().forEach(callExecutor::registerRetryListener);
        return callExecutor.execute(this.callable);
    }

}
