package com.evanlennick.retry4j;

import com.evanlennick.retry4j.exception.CallFailureException;
import com.evanlennick.retry4j.exception.UnexpectedCallFailureException;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class RetryExecutorTest {

    @Test(expectedExceptions = {CallFailureException.class})
    public void verifyReturningFalseFromCallFails() throws Exception {
        Callable<Boolean> callable = () -> false;

        RetryConfig retryConfig = new RetryConfigBuilder()
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0)
                .withFixedBackoff()
                .build();

        new RetryExecutor(retryConfig).execute(callable);
    }

    @Test
    public void verifyReturningTrueFromCallSucceeds() throws Exception {
        Callable<Boolean> callable = () -> true;

        RetryConfig retryConfig = new RetryConfigBuilder()
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0)
                .withFixedBackoff()
                .build();

        RetryResults results = new RetryExecutor(retryConfig).execute(callable);
        assertThat(results.wasSuccessful());
    }

    @Test(expectedExceptions = {CallFailureException.class})
    public void verifyExceptionFromCallThrowsCallFailureException() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new RuntimeException();
        };

        RetryConfig retryConfig = new RetryConfigBuilder()
                .retryOnAnyException()
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0)
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
                .withDelayBetweenTries(0)
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
                .withDelayBetweenTries(0)
                .withFixedBackoff()
                .build();

        new RetryExecutor(retryConfig).execute(callable);
    }

    @Test
    public void verifyResultsArePopulatedOnSuccessfulCall() throws Exception {
        Callable<Boolean> callable = () -> true;

        RetryConfig retryConfig = new RetryConfigBuilder()
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0)
                .withFixedBackoff()
                .build();

        RetryResults results = new RetryExecutor(retryConfig).execute(callable);

        assertThat(results.wasSuccessful());
        assertThat(results.getCallName()).isNotEmpty();
        assertThat(results.getTotalElapsedDuration().toMillis()).isCloseTo(0, within(25L));
        assertThat(results.getTotalTries()).isEqualTo(1);
    }

    @Test
    public void verifyResultsArePopulatedOnFailedCall() throws Exception {
        Callable<Boolean> callable = () -> false;

        RetryConfig retryConfig = new RetryConfigBuilder()
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0)
                .withFixedBackoff()
                .build();

        try {
            new RetryExecutor(retryConfig).execute(callable);
        } catch (CallFailureException e) {
            RetryResults results = e.getRetryResults();
            assertThat(results.wasSuccessful()).isFalse();
            assertThat(results.getCallName()).isNotEmpty();
            assertThat(results.getTotalElapsedDuration().toMillis()).isCloseTo(0, within(25L));
            assertThat(results.getTotalTries()).isEqualTo(5);
        }
    }
}
