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
    public void verifyRetryOnAnyExcludingThrowsCallFailureException() {
        Callable<Boolean> callable = () -> {
            throw new UnsupportedOperationException();
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryOnAnyExceptionExcluding(UnsupportedOperationException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        new CallExecutorBuilder().config(retryConfig).build().execute(callable);
    }

    @Test(expectedExceptions = {RetriesExhaustedException.class})
    public void verifyRetryOnAnyExcludingCallSucceeds() {
        Callable<Boolean> callable = () -> {
            throw new IllegalArgumentException();
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryOnAnyExceptionExcluding(UnsupportedOperationException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        new CallExecutorBuilder().config(retryConfig).build().execute(callable);
    }

    @Test(expectedExceptions = UnexpectedException.class)
    public void verifySubclassOfExcludedExceptionThrowsUnexpectedException() {
        Callable<Boolean> callable = () -> {
            throw new FileNotFoundException();
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryOnAnyExceptionExcluding(IOException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        new CallExecutorBuilder().config(retryConfig).build().execute(callable);
    }

    @Test(expectedExceptions = RetriesExhaustedException.class)
    public void verifySuperclassOfExcludedExceptionDoesntThrowUnexpectedException() {
        Callable<Boolean> callable = () -> {
            throw new IOException();
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryOnAnyExceptionExcluding(FileNotFoundException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        new CallExecutorBuilder().config(retryConfig).build().execute(callable);
    }

    @Test(expectedExceptions = {UnexpectedException.class})
    public void verifyMultipleExceptions_throwFirstExcludedException() {
        Callable<Boolean> callable = () -> {
            throw new IllegalArgumentException();
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryOnAnyExceptionExcluding(IllegalArgumentException.class, UnsupportedOperationException.class)
                .withMaxNumberOfTries(5)
                .withNoWaitBackoff()
                .build();

        new CallExecutorBuilder().config(retryConfig).build().execute(callable);
    }

    @Test(expectedExceptions = {UnexpectedException.class})
    public void verifyMultipleExceptions_throwSecondExcludedException() {
        Callable<Boolean> callable = () -> {
            throw new UnsupportedOperationException();
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryOnAnyExceptionExcluding(IllegalArgumentException.class, UnsupportedOperationException.class)
                .withMaxNumberOfTries(5)
                .withNoWaitBackoff()
                .build();

        new CallExecutorBuilder().config(retryConfig).build().execute(callable);
    }

    @Test(expectedExceptions = {RetriesExhaustedException.class})
    public void verifyMultipleExceptions_retryOnNotExcludedException() {
        Callable<Boolean> callable = () -> {
            throw new NullPointerException();
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryOnAnyExceptionExcluding(IllegalArgumentException.class, UnsupportedOperationException.class)
                .withMaxNumberOfTries(5)
                .withNoWaitBackoff()
                .build();

        new CallExecutorBuilder().config(retryConfig).build().execute(callable);
    }

}
