package com.evanlennick.retry4j.listener;

import com.evanlennick.retry4j.SyncCallExecutor;
import com.evanlennick.retry4j.CallResults;
import com.evanlennick.retry4j.RetryConfig;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class SyncCallExecutorBuilder<T> {

    private RetryConfig config;
    private ArrayList<RetryListener> retryListeners = new ArrayList<>();
    private Callable<T> callable;

    public static <T> SyncCallExecutorBuilder newSyncCall(Callable<T> callable) {
        return new SyncCallExecutorBuilder(callable);
    }

    private SyncCallExecutorBuilder(Callable<T> callable) {
        if (callable == null) throw new NullPointerException("Callable cannot be null.");
        this.callable = callable;
    }

    public SyncCallExecutorBuilder withConfig(RetryConfig config) {
        if (config == null) throw new NullPointerException("Retry configuration cannot be null.");
        this.config = config;
        return this;
    }

    public SyncCallExecutorBuilder afterFailedTry(AfterFailedTryListener afterFailedTryListener) {
        return addRetryListener(afterFailedTryListener);
    }

    public SyncCallExecutorBuilder beforeNextTry(BeforeNextTryListener beforeNextTryListener) {
        return addRetryListener(beforeNextTryListener);
    }

    public SyncCallExecutorBuilder onFailure(OnFailureListener onFailureListener) {
        return addRetryListener(onFailureListener);
    }

    public SyncCallExecutorBuilder onSuccess(OnSuccessListener onSuccessListener) {
        return addRetryListener(onSuccessListener);
    }

    private SyncCallExecutorBuilder addRetryListener(RetryListener retryListener) {
        if (retryListener == null) throw new NullPointerException("Retry listener configuration cannot be null.");
        this.retryListeners.add(retryListener);
        return this;
    }

    public CallResults<T> execute() {
        SyncCallExecutor<T> callExecutor = new SyncCallExecutor<>(config);
        retryListeners.stream().forEach(callExecutor::registerRetryListener);
        return callExecutor.execute(this.callable);
    }

}
