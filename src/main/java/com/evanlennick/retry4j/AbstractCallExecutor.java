package com.evanlennick.retry4j;

import com.evanlennick.retry4j.listener.AfterFailedTryListener;
import com.evanlennick.retry4j.listener.BeforeNextTryListener;
import com.evanlennick.retry4j.listener.OnFailureListener;
import com.evanlennick.retry4j.listener.OnSuccessListener;

public abstract class AbstractCallExecutor<T, S> implements CallExecutor<T, S> {

    protected RetryConfig config;

    protected AfterFailedTryListener afterFailedTryListener;

    protected BeforeNextTryListener beforeNextTryListener;

    protected OnFailureListener onFailureListener;

    protected OnSuccessListener onSuccessListener;

    protected CallResults<S> results = new CallResults<>();

    public AbstractCallExecutor() {
        this(RetryConfigBuilder.newConfig().fixedBackoff5Tries10Sec().build());
    }

    public AbstractCallExecutor(RetryConfig config) {
        this.config = config;
    }


}
