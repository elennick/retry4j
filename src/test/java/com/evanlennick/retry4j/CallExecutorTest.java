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
import java.util.Random;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.within;

public class CallExecutorTest {

    private RetryConfigBuilder<Boolean> retryConfigBuilder;

    @BeforeMethod
    public void setup() {
        boolean configValidationEnabled = false;
        this.retryConfigBuilder = new RetryConfigBuilder<>(configValidationEnabled);
    }

    @Test
    public void verifyReturningObjectFromCallSucceeds() {
        Callable<Boolean> callable = () -> true;

        RetryConfig<Boolean> retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        Status status = new CallExecutor<>(retryConfig).execute(callable);
        assertThat(status.wasSuccessful()).isTrue();
    }

    @Test(expectedExceptions = {RetriesExhaustedException.class})
    public void verifyExceptionFromCallThrowsCallFailureException() {
        Callable<Boolean> callable = () -> {
            throw new RuntimeException();
        };

        RetryConfig<Boolean> retryConfig = retryConfigBuilder
                .retryOnAnyException()
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        new CallExecutor<>(retryConfig).execute(callable);
    }

    @Test(expectedExceptions = {UnexpectedException.class})
    public void verifySpecificSuperclassExceptionThrowsUnexpectedException() {
        Callable<Boolean> callable = () -> {
            throw new Exception();
        };

        RetryConfig<Boolean> retryConfig = retryConfigBuilder
                .retryOnSpecificExceptions(IOException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        new CallExecutor<>(retryConfig).execute(callable);
    }

    @Test(expectedExceptions = {RetriesExhaustedException.class})
    public void verifySpecificSubclassExceptionRetries() {
        Callable<Boolean> callable = () -> {
            throw new IOException();
        };

        RetryConfig<Boolean> retryConfig = retryConfigBuilder
                .retryOnSpecificExceptions(Exception.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        new CallExecutor<>(retryConfig).execute(callable);
    }

    @Test(expectedExceptions = {RetriesExhaustedException.class})
    public void verifyExactSameSpecificExceptionThrowsCallFailureException() {
        Callable<Boolean> callable = () -> {
            throw new IllegalArgumentException();
        };

        RetryConfig<Boolean> retryConfig = retryConfigBuilder
                .retryOnSpecificExceptions(IllegalArgumentException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        new CallExecutor<>(retryConfig).execute(callable);
    }

    @Test(expectedExceptions = {UnexpectedException.class})
    public void verifyUnspecifiedExceptionCausesUnexpectedCallFailureException() {
        Callable<Boolean> callable = () -> {
            throw new IllegalArgumentException();
        };

        RetryConfig<Boolean> retryConfig = retryConfigBuilder
                .retryOnSpecificExceptions(UnsupportedOperationException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        new CallExecutor<>(retryConfig).execute(callable);
    }

    @Test
    public void verifyStatusIsPopulatedOnSuccessfulCall() {
        Callable<Boolean> callable = () -> true;

        RetryConfig<Boolean> retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        Status status = new CallExecutor<>(retryConfig).execute(callable);

        assertThat(status.getResult()).isNotNull();
        assertThat(status.wasSuccessful()).isTrue();
        assertThat(status.getCallName()).isNullOrEmpty();
        assertThat(status.getTotalElapsedDuration().toMillis()).isCloseTo(0, within(25L));
        assertThat(status.getTotalTries()).isEqualTo(1);
    }

    @Test
    public void verifyStatusIsPopulatedOnFailedCall() {
        Callable<Boolean> callable = () -> { throw new FileNotFoundException(); };

        RetryConfig<Boolean> retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(5)
                .retryOnAnyException()
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        try {
            new CallExecutor<>(retryConfig).execute(callable, "TestCall");
            fail("RetriesExhaustedException wasn't thrown!");
        } catch (RetriesExhaustedException e) {
            Status status = e.getStatus();
            assertThat(status.getResult()).isNull();
            assertThat(status.wasSuccessful()).isFalse();
            assertThat(status.getCallName()).isEqualTo("TestCall");
            assertThat(status.getTotalElapsedDuration().toMillis()).isCloseTo(0, within(25L));
            assertThat(status.getTotalTries()).isEqualTo(5);
            assertThat(e.getCause()).isExactlyInstanceOf(FileNotFoundException.class);
        }
    }

    @Test
    public void verifyReturningObjectFromCallable() {
        Callable<Boolean> callable = () -> false;

        RetryConfig<Boolean> retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .build();

        Status status = new CallExecutor<>(retryConfig).execute(callable);

        assertThat(status.getResult()).isEqualTo(false);
    }

    @Test
    public void verifyNullCallResultCountsAsValidResult() {
        Callable<Boolean> callable = () -> null;

        RetryConfig<Boolean> retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .build();

        try {
            new CallExecutor<>(retryConfig).execute(callable);
        } catch (RetriesExhaustedException e) {
            Status status = e.getStatus();
            assertThat(status.getResult()).isNull();
            assertThat(status.wasSuccessful()).isTrue();
        }
    }

    @Test
    public void verifyRetryingIndefinitely() {
        Callable<Boolean> callable = () -> {
            Random random = new Random();
            if (random.nextInt(10000) == 0) {
                return true;
            }
            throw new IllegalArgumentException();
        };

        RetryConfig<Boolean> retryConfig = retryConfigBuilder
                .retryIndefinitely()
                .retryOnAnyException()
                .withFixedBackoff()
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .build();

        try {
            CallExecutor<Boolean> executor = new CallExecutor<>(retryConfig);
            executor.execute(callable);
        } catch (RetriesExhaustedException e) {
            fail("Retries should never be exhausted!");
        }
    }
}
