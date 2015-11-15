package com.evanlennick.retry4j;

import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class RetryExecutorTest {

    private RetryConfigBuilder retryConfigBuilder;

    @BeforeMethod
    public void setup() {
        boolean configValidationEnabled = false;
        this.retryConfigBuilder = new RetryConfigBuilder(configValidationEnabled);
    }

    @Test
    public void verifyReturningObjectFromCallSucceeds() throws Exception {
        Callable<Boolean> callable = () -> true;

        RetryConfig retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0)
                .withFixedBackoff()
                .build();

        CallResults results = new CallExecutor(retryConfig).execute(callable);
        assertThat(results.wasSuccessful());
    }

    @Test(expectedExceptions = {RetriesExhaustedException.class})
    public void verifyExceptionFromCallThrowsCallFailureException() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new RuntimeException();
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryOnAnyException()
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0)
                .withFixedBackoff()
                .build();

        new CallExecutor(retryConfig).execute(callable);
    }

    @Test(expectedExceptions = {RetriesExhaustedException.class})
    public void verifySpecificExceptionFromCallThrowsCallFailureException() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new IllegalArgumentException();
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryOnSpecificExceptions(IllegalArgumentException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0)
                .withFixedBackoff()
                .build();

        new CallExecutor(retryConfig).execute(callable);
    }

    @Test(expectedExceptions = {UnexpectedException.class})
    public void verifyUnspecifiedExceptionCausesUnexpectedCallFailureException() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new IllegalArgumentException();
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryOnSpecificExceptions(UnsupportedOperationException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0)
                .withFixedBackoff()
                .build();

        new CallExecutor(retryConfig).execute(callable);
    }

    @Test
    public void verifyResultsArePopulatedOnSuccessfulCall() throws Exception {
        Callable<Boolean> callable = () -> true;

        RetryConfig retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0)
                .withFixedBackoff()
                .build();

        CallResults results = new CallExecutor(retryConfig).execute(callable);

        assertThat(results.getResult()).isNotNull();
        assertThat(results.wasSuccessful());
        assertThat(results.getCallName()).isNotEmpty();
        assertThat(results.getTotalElapsedDuration().toMillis()).isCloseTo(0, within(25L));
        assertThat(results.getTotalTries()).isEqualTo(1);
    }

    @Test
    public void verifyResultsArePopulatedOnFailedCall() throws Exception {
        Callable<Boolean> callable = () -> false;

        RetryConfig retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0)
                .withFixedBackoff()
                .build();

        try {
            new CallExecutor(retryConfig).execute(callable);
        } catch (RetriesExhaustedException e) {
            CallResults results = e.getCallResults();
            assertThat(results.getResult()).isNull();
            assertThat(results.wasSuccessful()).isFalse();
            assertThat(results.getCallName()).isNotEmpty();
            assertThat(results.getTotalElapsedDuration().toMillis()).isCloseTo(0, within(25L));
            assertThat(results.getTotalTries()).isEqualTo(5);
        }
    }

    @Test
    public void verifyReturningObjectFromCallable() throws Exception {
        Callable<String> callable = () -> "test";

        RetryConfig retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0)
                .build();

        CallResults results = new CallExecutor(retryConfig).execute(callable);

        assertThat(results.getResult()).isEqualTo("test");
    }
}
