package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AsyncCallExecutorTest {

    private RetryConfig retryOnAnyExceptionConfig;

    private RetryConfig failOnAnyExceptionConfig;

    @BeforeClass
    public void setup() {
        retryOnAnyExceptionConfig = new RetryConfigBuilder()
                .retryOnAnyException()
                .withFixedBackoff()
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(Duration.ofMillis(0))
                .build();

        failOnAnyExceptionConfig = new RetryConfigBuilder()
                .failOnAnyException()
                .withFixedBackoff()
                .withMaxNumberOfTries(1)
                .withDelayBetweenTries(Duration.ofMillis(0))
                .build();
    }

    @Test
    public void verifyMultipleCalls() throws Exception {
        Callable<Boolean> callable = () -> true;

        AsyncCallExecutor<Boolean> executor = new AsyncCallExecutor<>(retryOnAnyExceptionConfig);

        CompletableFuture<Status<Boolean>> future1 = executor.execute(callable);
        CompletableFuture<Status<Boolean>> future2 = executor.execute(callable);
        CompletableFuture<Status<Boolean>> future3 = executor.execute(callable);

        CompletableFuture combinedFuture
                = CompletableFuture.allOf(future1, future2, future3);
        combinedFuture.get();

        assertThat(future1).isDone();
        assertThat(future2).isDone();
        assertThat(future3).isDone();
    }

    @Test
    public void verifyOneCall_success() throws Exception {
        Callable<Boolean> callable = () -> true;

        AsyncCallExecutor<Boolean> executor = new AsyncCallExecutor<>(retryOnAnyExceptionConfig);

        CompletableFuture<Status<Boolean>> future = executor.execute(callable);

        Status<Boolean> status = future.get();
        assertThat(future).isDone();
        assertThat(status.wasSuccessful()).isTrue();
    }

    @Test
    public void verifyOneCall_failDueToTooManyRetries() throws Exception {
        Callable<Boolean> callable = () -> { throw new RuntimeException(); };

        AsyncCallExecutor<Boolean> executor = new AsyncCallExecutor<>(retryOnAnyExceptionConfig);

        CompletableFuture<Status<Boolean>> future = executor.execute(callable);

        assertThatThrownBy(future::get)
                .isExactlyInstanceOf(ExecutionException.class)
                .hasCauseExactlyInstanceOf(RetriesExhaustedException.class);
    }

    @Test
    public void verifyOneCall_failDueToUnexpectedException() throws Exception {
        Callable<Boolean> callable = () -> { throw new RuntimeException(); };

        AsyncCallExecutor<Boolean> executor = new AsyncCallExecutor<>(failOnAnyExceptionConfig);

        CompletableFuture<Status<Boolean>> future = executor.execute(callable);

        assertThatThrownBy(future::get)
                .isExactlyInstanceOf(ExecutionException.class)
                .hasCauseExactlyInstanceOf(UnexpectedException.class);
    }
}
