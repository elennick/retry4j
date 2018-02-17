package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;

public class CallExecutorTest_RetryOnAnyExcludingTest {

    private RetryConfigBuilder retryConfigBuilder;

    @BeforeMethod
    public void setup() {
        boolean configValidationEnabled = false;
        this.retryConfigBuilder = new RetryConfigBuilder(configValidationEnabled);
    }

    @Test(expectedExceptions = {UnexpectedException.class})
    public void verifyRetryOnAnyExcludingThrowsCallFailureException() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new UnsupportedOperationException();
        };

        RetryConfig config = retryConfigBuilder
                .retryOnAnyExceptionExcluding(UnsupportedOperationException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        CallExecutor.<Boolean>builder()
                .withConfig(config)
                .build()
                .execute(callable);
    }

    @Test(expectedExceptions = {RetriesExhaustedException.class})
    public void verifyRetryOnAnyExcludingCallSucceeds() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new IllegalArgumentException();
        };

        RetryConfig config = retryConfigBuilder
                .retryOnAnyExceptionExcluding(UnsupportedOperationException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        CallExecutor.<Boolean>builder()
                .withConfig(config)
                .build()
                .execute(callable);
    }

    @Test(expectedExceptions = UnexpectedException.class)
    public void verifySubclassOfExcludedExceptionThrowsUnexpectedException() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new FileNotFoundException();
        };

        RetryConfig config = retryConfigBuilder
                .retryOnAnyExceptionExcluding(IOException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        CallExecutor.<Boolean>builder()
                .withConfig(config)
                .build()
                .execute(callable);
    }

    @Test(expectedExceptions = RetriesExhaustedException.class)
    public void verifySuperclassOfExcludedExceptionDoesntThrowUnexpectedException() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new IOException();
        };

        RetryConfig config = retryConfigBuilder
                .retryOnAnyExceptionExcluding(FileNotFoundException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        CallExecutor.<Boolean>builder()
                .withConfig(config)
                .build()
                .execute(callable);
    }

}
