package com.evanlennick.retry4j;

import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.evanlennick.retry4j.listener.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public interface CallExecutor<T, S> {

    T execute(Callable<S> callable) throws RetriesExhaustedException, UnexpectedException;

    T execute(Callable<S> callable, RetryConfig config) throws RetriesExhaustedException, UnexpectedException ;
}
