package com.evanlennick.retry4j;

import com.evanlennick.retry4j.handlers.AfterFailedTryListener;
import com.evanlennick.retry4j.handlers.BeforeNextTryListener;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;

public class RetryListenerTest {

    @Ignore //TODO implement this test
    @Test
    public void verifyAfterFailedTryListener() throws Exception {
        Callable<String> callable = () -> "test";

        RetryConfig retryConfig = new RetryConfigBuilder()
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0)
                .build();

        RetryExecutor executor = new RetryExecutor(retryConfig);
        executor.registerRetryListener((AfterFailedTryListener) results -> {

        });

        CallResults results = executor.execute(callable);
    }

    @Ignore //TODO implement this test
    @Test
    public void verifyBeforeNextTryListener() throws Exception {
        Callable<String> callable = () -> "test";

        RetryConfig retryConfig = new RetryConfigBuilder()
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0)
                .build();

        RetryExecutor executor = new RetryExecutor(retryConfig);
        executor.registerRetryListener((BeforeNextTryListener) results -> {

        });

        CallResults results = executor.execute(callable);
    }
}
