package com.evanlennick.retry4j;

import java.util.concurrent.Callable;

public interface RetryExecutor<T, S> {

    S execute(Callable<T> callable);

    S execute(Callable<T> callable, String callName);

}
