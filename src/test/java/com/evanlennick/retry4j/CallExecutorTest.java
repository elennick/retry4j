package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.exception.InvalidRetryConfigException;
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

    private RetryConfig.RetryConfigBuilder retryConfigBuilder;

    @BeforeMethod
    public void setup() {
        this.retryConfigBuilder = RetryConfig.builder().withValidationDisabled();
    }

    @Test
    public void verifyReturningObjectFromCallSucceeds() throws Exception {
        Callable<Boolean> callable = () -> true;

        RetryConfig retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        Status status = CallExecutor.<Boolean>builder()
                .withConfig(retryConfig)
                .build()
                .execute(callable);

        assertThat(status.wasSuccessful()).isTrue();
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

        CallExecutor.<Boolean>builder()
                .withConfig(retryConfig)
                .build()
                .execute(callable);
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

        CallExecutor.<Boolean>builder()
                .withConfig(retryConfig)
                .build()
                .execute(callable);
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

        CallExecutor.<Boolean>builder()
                .withConfig(retryConfig)
                .build()
                .execute(callable);
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

        CallExecutor.<Boolean>builder()
                .withConfig(retryConfig)
                .build()
                .execute(callable);
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

        CallExecutor.<Boolean>builder()
                .withConfig(retryConfig)
                .build()
                .execute(callable);
    }

    @Test
    public void verifyStatusIsPopulatedOnSuccessfulCall() throws Exception {
        Callable<Boolean> callable = () -> true;

        RetryConfig retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        Status<Boolean> status = CallExecutor.<Boolean>builder()
                .withConfig(retryConfig)
                .build()
                .execute(callable);

        assertThat(status.getResult()).isNotNull();
        assertThat(status.wasSuccessful()).isTrue();
        assertThat(status.getCallName()).isNullOrEmpty();
        assertThat(status.getTotalElapsedDuration().toMillis()).isCloseTo(0, within(25L));
        assertThat(status.getTotalTries()).isEqualTo(1);
    }

    @Test
    public void verifyStatusIsPopulatedOnFailedCall() throws Exception {
        Callable<Boolean> callable = () -> { throw new FileNotFoundException(); };

        RetryConfig retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(5)
                .retryOnAnyException()
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        try {
            Status<Boolean> status = CallExecutor.<Boolean>builder()
                    .withConfig(retryConfig)
                    .build()
                    .execute(callable, "TestCall");
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
    public void verifyReturningObjectFromCallable() throws Exception {
        Callable<String> callable = () -> "test";

        RetryConfig retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .build();

        Status<String> status = CallExecutor.<String>builder()
                .withConfig(retryConfig)
                .build()
                .execute(callable, "TestCall");

        assertThat(status.getResult()).isEqualTo("test");
    }

    @Test
    public void verifyNullCallResultCountsAsValidResult() throws Exception {
        Callable<String> callable = () -> null;

        RetryConfig retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .build();

        try {
            Status<String> status = CallExecutor.<String>builder()
                    .withConfig(retryConfig)
                    .build()
                    .execute(callable);
        } catch (RetriesExhaustedException e) {
            Status status = e.getStatus();
            assertThat(status.getResult()).isNull();
            assertThat(status.wasSuccessful()).isTrue();
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
            CallExecutor.<Boolean>builder()
                    .withConfig(retryConfig)
                    .build()
                    .execute(callable);
        } catch (RetriesExhaustedException e) {
            fail("Retries should never be exhausted!");
        }
    }

    @Test(expectedExceptions = InvalidRetryConfigException.class)
    public void verifyNoConfigThrowsException() throws Exception {
        Callable<String> callable = () -> "blah";

        CallExecutor.<String>builder()
                .build()
                .execute(callable);
    }
}
