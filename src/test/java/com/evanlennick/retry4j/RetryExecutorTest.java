package com.evanlennick.retry4j;

import com.evanlennick.retry4j.exception.CallFailureException;
import com.evanlennick.retry4j.exception.UnexpectedCallFailureException;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class RetryExecutorTest {

    @Test(expectedExceptions = {CallFailureException.class})
    public void verifyReturningFalseFromCallFails() throws Exception {
        Callable<Boolean> callable = () -> false;

        RetryConfig retryConfig = new RetryConfigBuilder()
                .withMaxNumberOfTries(5)
                .withDurationBetweenTries(0)
                .withFixedBackoff()
                .build();

        new RetryExecutor(retryConfig).execute(callable);
    }

    @Test
    public void verifyReturningTrueFromCallSucceeds() throws Exception {
        Callable<Boolean> callable = () -> true;

        RetryConfig retryConfig = new RetryConfigBuilder()
                .withMaxNumberOfTries(5)
                .withDurationBetweenTries(0)
                .withFixedBackoff()
                .build();

        RetryResults results = new RetryExecutor(retryConfig).execute(callable);
        assertThat(results.isSucceeded());
    }

    @Test(expectedExceptions = {CallFailureException.class})
    public void verifyExceptionFromCallThrowsCallFailureException() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new RuntimeException();
        };

        RetryConfig retryConfig = new RetryConfigBuilder()
                .retryOnAnyException()
                .withMaxNumberOfTries(1)
                .withDurationBetweenTries(0)
                .withFixedBackoff()
                .build();

        new RetryExecutor(retryConfig).execute(callable);
    }

    @Test(expectedExceptions = {CallFailureException.class})
    public void verifySpecificExceptionFromCallThrowsCallFailureException() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new IllegalArgumentException();
        };

        RetryConfig retryConfig = new RetryConfigBuilder()
                .retryOnSpecificExceptions(IllegalArgumentException.class)
                .withMaxNumberOfTries(1)
                .withDurationBetweenTries(0)
                .withFixedBackoff()
                .build();

        new RetryExecutor(retryConfig).execute(callable);
    }

    @Test(expectedExceptions = {UnexpectedCallFailureException.class})
    public void verifyUnspecifiedExceptionCausesUnexpectedCallFailureException() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new IllegalArgumentException();
        };

        RetryConfig retryConfig = new RetryConfigBuilder()
                .retryOnSpecificExceptions(UnsupportedOperationException.class)
                .withMaxNumberOfTries(1)
                .withDurationBetweenTries(0)
                .withFixedBackoff()
                .build();

        new RetryExecutor(retryConfig).execute(callable);
    }

    @Test
    public void verifyFixedBackoffTiming() throws Exception {
        Callable<Boolean> callable = () -> false;

        RetryConfig retryConfig = new RetryConfigBuilder()
                .withMaxNumberOfTries(3)
                .withDurationBetweenTries(100L)
                .withFixedBackoff()
                .build();

        long start = System.currentTimeMillis();

        try {
            new RetryExecutor(retryConfig).execute(callable);
        } catch (CallFailureException | UnexpectedCallFailureException e) {
        }

        long end = System.currentTimeMillis();
        long elapsed = end - start;

        assertThat(elapsed).isCloseTo(300, within(25L));
    }

    @Test
    public void verifyExponentialBackoffTiming() throws Exception {
        Callable<Boolean> callable = () -> false;

        RetryConfig retryConfig = new RetryConfigBuilder()
                .withMaxNumberOfTries(5)
                .withDurationBetweenTries(20L)
                .withExponentialBackoff()
                .build();

        long start = System.currentTimeMillis();

        try {
            new RetryExecutor(retryConfig).execute(callable);
        } catch (CallFailureException | UnexpectedCallFailureException e) {
        }

        long end = System.currentTimeMillis();
        long elapsed = end - start;

        assertThat(elapsed).isCloseTo(290, within(25L));
    }

    @Test
    public void verifyResultsArePopulatedOnSuccessfulCall() throws Exception {
        Callable<Boolean> callable = () -> true;

        RetryConfig retryConfig = new RetryConfigBuilder()
                .withMaxNumberOfTries(5)
                .withDurationBetweenTries(0)
                .withFixedBackoff()
                .build();

        RetryResults results = new RetryExecutor(retryConfig).execute(callable);

        assertThat(results.isSucceeded());
        assertThat(results.getCallName()).isNotEmpty();
        assertThat(results.getTotalDurationElapsed().toMillis()).isCloseTo(0, within(25L));
        assertThat(results.getTotalTries()).isEqualTo(1);
    }

    @Test
    public void verifyResultsArePopulatedOnFailedCall() throws Exception {
        Callable<Boolean> callable = () -> false;

        RetryConfig retryConfig = new RetryConfigBuilder()
                .withMaxNumberOfTries(5)
                .withDurationBetweenTries(0)
                .withFixedBackoff()
                .build();

        try {
            new RetryExecutor(retryConfig).execute(callable);
        } catch (CallFailureException e) {
            RetryResults results = e.getRetryResults();
            assertThat(results.isSucceeded());
            assertThat(results.getCallName()).isNotEmpty();
            assertThat(results.getTotalDurationElapsed().toMillis()).isCloseTo(0, within(25L));
            assertThat(results.getTotalTries()).isEqualTo(5);
        }
    }
}
