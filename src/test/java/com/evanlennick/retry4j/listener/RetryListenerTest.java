package com.evanlennick.retry4j.listener;

import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.CallResults;
import com.evanlennick.retry4j.RetryConfig;
import com.evanlennick.retry4j.RetryConfigBuilder;
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

        CallExecutor executor = new CallExecutor(retryConfig);
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

        CallExecutor executor = new CallExecutor(retryConfig);
        executor.registerRetryListener((BeforeNextTryListener) results -> {

        });

        CallResults results = executor.execute(callable);
    }
}
