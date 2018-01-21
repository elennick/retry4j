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
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
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
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        new CallExecutor(retryConfig).execute(callable);
    }

    @Test(expectedExceptions = {UnexpectedException.class})
    public void verifySpecificSuperclassExceptionThrowsUnexpectedException() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new Exception();
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryOnSpecificExceptions(IOException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        new CallExecutor(retryConfig).execute(callable);
    }

    @Test(expectedExceptions = {RetriesExhaustedException.class})
    public void verifySpecificSubclassExceptionRetries() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new IOException();
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryOnSpecificExceptions(Exception.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        new CallExecutor(retryConfig).execute(callable);
    }

    @Test(expectedExceptions = {RetriesExhaustedException.class})
    public void verifyExactSameSpecificExceptionThrowsCallFailureException() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new IllegalArgumentException();
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryOnSpecificExceptions(IllegalArgumentException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
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
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        new CallExecutor(retryConfig).execute(callable);
    }

    @Test
    public void verifyResultsArePopulatedOnSuccessfulCall() throws Exception {
        Callable<Boolean> callable = () -> true;

        RetryConfig retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
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
        Callable<Boolean> callable = () -> { throw new FileNotFoundException(); };

        RetryConfig retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(5)
                .retryOnAnyException()
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        try {
            new CallExecutor(retryConfig).execute(callable);
            fail("RetriesExhaustedException wasn't thrown!");
        } catch (RetriesExhaustedException e) {
            CallResults results = e.getCallResults();
            assertThat(results.getResult()).isNull();
            assertThat(results.wasSuccessful()).isFalse();
            assertThat(results.getCallName()).isNotEmpty();
            assertThat(results.getTotalElapsedDuration().toMillis()).isCloseTo(0, within(25L));
            assertThat(results.getTotalTries()).isEqualTo(5);
            assertThat(e.getCause()).isExactlyInstanceOf(FileNotFoundException.class);
        }
    }

    @Test
    public void verifyReturningObjectFromCallable() throws Exception {
        Callable<String> callable = () -> "test";

        RetryConfig retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .build();

        CallResults results = new CallExecutor(retryConfig).execute(callable);

        assertThat(results.getResult()).isEqualTo("test");
    }

    @Test
    public void verifyNullCallResultCountsAsValidResult() throws Exception {
        Callable<String> callable = () -> null;

        RetryConfig retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .build();

        try {
            new CallExecutor(retryConfig).execute(callable);
        } catch (RetriesExhaustedException e) {
            CallResults results = e.getCallResults();
            assertThat(results.getResult()).isNull();
            assertThat(results.wasSuccessful()).isTrue();
        }
    }

    @Test
    public void verifyRetryingIndefinitely() throws Exception {
        Callable<Boolean> callable = () -> {
            Random random = new Random();
            if (random.nextInt(10000) == 0) {
                return true;
            }
            throw new IllegalArgumentException();
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryIndefinitely()
                .retryOnAnyException()
                .withFixedBackoff()
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .build();

        try {
            CallExecutor executor = new CallExecutor(retryConfig);
            executor.execute(callable);
        } catch (RetriesExhaustedException e) {
            fail("Retries should never be exhausted!");
        }
    }
}
