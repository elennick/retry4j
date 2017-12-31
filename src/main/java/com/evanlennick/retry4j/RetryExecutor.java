package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.evanlennick.retry4j.listener.RetryListener;

import java.util.List;
import java.util.concurrent.Callable;

public interface RetryExecutor<T> {

    Object execute(Callable<T> callable) throws RetriesExhaustedException, UnexpectedException;

    void registerRetryListener(RetryListener listener);

    void registerRetryListeners(List<RetryListener> listeners);

    void setConfig(RetryConfig config);

}
