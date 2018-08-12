package com.evanlennick.retry4j;

import com.evanlennick.retry4j.backoff.BackoffStrategy;
import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CallExecutorTest {

    @Mock
    private BackoffStrategy mockBackOffStrategy;

    private RetryConfigBuilder retryConfigBuilder;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);

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

        Status status = new CallExecutor(retryConfig).execute(callable);
        assertThat(status.wasSuccessful());
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

    @Test(expectedExceptions = {RetriesExhaustedException.class})
    public void shouldMatchExceptionCauseAndRetry() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new Exception(new CustomTestException("message", 3));
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryOnCausedBy()
                .retryOnSpecificExceptions(CustomTestException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        new CallExecutor(retryConfig).execute(callable);
    }


    @Test(expectedExceptions = {RetriesExhaustedException.class})
    public void shouldMatchExceptionCauseAtGreaterThanALevelDeepAndRetry() throws Exception {
        class CustomException extends Exception {
            CustomException(Throwable cause){
                super(cause);
            }
        }
        Callable<Boolean> callable = () -> {
            throw new Exception(new CustomException(new RuntimeException(new IOException())));
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryOnCausedBy()
                .retryOnSpecificExceptions(IOException.class)
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        new CallExecutor(retryConfig).execute(callable);
    }

    @Test(expectedExceptions = {UnexpectedException.class})
    public void shouldThrowUnexpectedIfThrownExceptionCauseDoesNotMatchRetryExceptions() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new Exception(new CustomTestException("message", 3));
        };

        RetryConfig retryConfig = retryConfigBuilder
                .retryOnCausedBy()
                .retryOnSpecificExceptions(IOException.class)
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
    public void verifyStatusIsPopulatedOnSuccessfulCall() throws Exception {
        Callable<Boolean> callable = () -> true;

        RetryConfig retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        Status status = new CallExecutor(retryConfig).execute(callable);

        assertThat(status.getResult()).isNotNull();
        assertThat(status.wasSuccessful());
        assertThat(status.getCallName()).isNullOrEmpty();
        assertThat(status.getTotalElapsedDuration().toMillis()).isCloseTo(0, within(25L));
        assertThat(status.getTotalTries()).isEqualTo(1);
    }

    @Test
    public void verifyStatusIsPopulatedOnFailedCall() throws Exception {
        Callable<Boolean> callable = () -> {
            throw new FileNotFoundException();
        };

        RetryConfig retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(5)
                .retryOnAnyException()
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        try {
            new CallExecutor(retryConfig).execute(callable, "TestCall");
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

        Status status = new CallExecutor(retryConfig).execute(callable);

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
            new CallExecutor(retryConfig).execute(callable);
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
            CallExecutor executor = new CallExecutor(retryConfig);
            executor.execute(callable);
        } catch (RetriesExhaustedException e) {
            fail("Retries should never be exhausted!");
        }
    }

    @Test
    public void verifyRetryPolicyTimeoutIsUsed() {
        Callable<Object> callable = () -> {
            throw new RuntimeException();
        };

        Duration delayBetweenTriesDuration = Duration.ofSeconds(17);
        when(mockBackOffStrategy.getDurationToWait(1, delayBetweenTriesDuration)).thenReturn(Duration.ofSeconds(5));

        RetryConfig retryConfig = retryConfigBuilder
                .withMaxNumberOfTries(2)
                .retryOnAnyException()
                .withDelayBetweenTries(delayBetweenTriesDuration)
                .withBackoffStrategy(mockBackOffStrategy)
                .build();

        final long before = System.currentTimeMillis();
        try {
            new CallExecutor<>(retryConfig).execute(callable);
        } catch (RetriesExhaustedException ignored) {
        }

        assertThat(System.currentTimeMillis() - before).isGreaterThan(5000);
        verify(mockBackOffStrategy).getDurationToWait(1, delayBetweenTriesDuration);
    }

    @Test
    public void verifyNoDurationSpecifiedSucceeds() {
        Callable<String> callable = () -> "test";

        RetryConfig noWaitConfig = new RetryConfigBuilder()
                .withMaxNumberOfTries(1)
                .withNoWaitBackoff()
                .build();

        Status status = new CallExecutor(noWaitConfig).execute(callable);

        assertThat(status.getResult()).isEqualTo("test");
    }
}
