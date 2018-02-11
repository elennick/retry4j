package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;

import java.util.concurrent.Callable;

public interface RetryExecutor<T, S> {

    S execute(Callable<T> callable) throws RetriesExhaustedException, UnexpectedException;

    S execute(Callable<T> callable, String callName) throws RetriesExhaustedException, UnexpectedException;

    void setConfig(RetryConfig config);

}
