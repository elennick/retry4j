package com.evanlennick.retry4j;

import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;

import java.util.concurrent.Callable;

public interface CallExecutor<T, S> {

    T execute(Callable<S> callable) throws RetriesExhaustedException, UnexpectedException;

    T execute(Callable<S> callable, RetryConfig config) throws RetriesExhaustedException, UnexpectedException ;
}
