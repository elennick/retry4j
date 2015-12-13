package com.evanlennick.retry4j.listener;

import com.evanlennick.retry4j.SyncCallExecutor;

import java.util.ArrayList;

public class SyncCallExecutorBuilder {

    private ArrayList<RetryListener> retryListeners = new ArrayList<>();

    public static SyncCallExecutorBuilder newSyncCallExecutorBuilder() {
        return new SyncCallExecutorBuilder();
    }

    private SyncCallExecutorBuilder() {

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

    public <T> SyncCallExecutor<T> build() {
        SyncCallExecutor<T> callExecutor = new SyncCallExecutor<>();
        retryListeners.stream().forEach(callExecutor::registerRetryListener);
        return callExecutor;
    }

}
