package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AsyncCallExecutorTest {

    private RetryConfig retryOnAnyExceptionConfig;

    private RetryConfig failOnAnyExceptionConfig;

    private ExecutorService executorService;

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

        executorService = Executors.newFixedThreadPool(5);
    }

    @AfterClass
    public void teardown() {
        executorService.shutdown();
    }

    @Test
    public void verifyMultipleCalls_noExecutorService() throws Exception {
        Callable<Boolean> callable = () -> true;

        AsyncCallExecutor<Boolean> executor = new CallExecutorBuilder().config(retryOnAnyExceptionConfig).buildAsync();

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
    public void verifyOneCall_success_noExecutorService() throws Exception {
        Callable<Boolean> callable = () -> true;

        AsyncCallExecutor<Boolean> executor = new CallExecutorBuilder().config(retryOnAnyExceptionConfig).buildAsync();

        CompletableFuture<Status<Boolean>> future = executor.execute(callable);

        Status<Boolean> status = future.get();
        assertThat(future).isDone();
        assertThat(status.wasSuccessful()).isTrue();
    }

    @Test
    public void verifyOneCall_failDueToTooManyRetries_noExecutorService() throws Exception {
        Callable<Boolean> callable = () -> { throw new RuntimeException(); };

        AsyncCallExecutor<Boolean> executor = new CallExecutorBuilder().config(retryOnAnyExceptionConfig).buildAsync();

        CompletableFuture<Status<Boolean>> future = executor.execute(callable);

        assertThatThrownBy(future::get)
                .isExactlyInstanceOf(ExecutionException.class)
                .hasCauseExactlyInstanceOf(RetriesExhaustedException.class);
    }

    @Test
    public void verifyOneCall_failDueToUnexpectedException_noExecutorService() throws Exception {
        Callable<Boolean> callable = () -> { throw new RuntimeException(); };

        AsyncCallExecutor<Boolean> executor = new CallExecutorBuilder().config(failOnAnyExceptionConfig).buildAsync();

        CompletableFuture<Status<Boolean>> future = executor.execute(callable);

        assertThatThrownBy(future::get)
                .isExactlyInstanceOf(ExecutionException.class)
                .hasCauseExactlyInstanceOf(UnexpectedException.class);
    }

    @Test
    public void verifyMultipleCalls_withExecutorService() throws Exception {
        Callable<Boolean> callable = () -> true;

        AsyncCallExecutor<Boolean> executor = new CallExecutorBuilder().config(retryOnAnyExceptionConfig)
            .buildAsync(executorService);

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
    public void verifyOneCall_success_withExecutorService() throws Exception {
        Callable<Boolean> callable = () -> true;

        AsyncCallExecutor<Boolean> executor = new CallExecutorBuilder().config(retryOnAnyExceptionConfig)
            .buildAsync(executorService);

        CompletableFuture<Status<Boolean>> future = executor.execute(callable);

        Status<Boolean> status = future.get();
        assertThat(future).isDone();
        assertThat(status.wasSuccessful()).isTrue();
    }

    @Test
    public void verifyOneCall_failDueToTooManyRetries_withExecutorService() throws Exception {
        Callable<Boolean> callable = () -> { throw new RuntimeException(); };

        AsyncCallExecutor<Boolean> executor = new CallExecutorBuilder().config(retryOnAnyExceptionConfig)
            .buildAsync(executorService);

        CompletableFuture<Status<Boolean>> future = executor.execute(callable);

        assertThatThrownBy(future::get)
                .isExactlyInstanceOf(ExecutionException.class)
                .hasCauseExactlyInstanceOf(RetriesExhaustedException.class);
    }

    @Test
    public void verifyOneCall_failDueToUnexpectedException_withExecutorService() throws Exception {
        Callable<Boolean> callable = () -> { throw new RuntimeException(); };

        AsyncCallExecutor<Boolean> executor = new CallExecutorBuilder().config(failOnAnyExceptionConfig)
            .buildAsync(executorService);

        CompletableFuture<Status<Boolean>> future = executor.execute(callable);

        assertThatThrownBy(future::get)
                .isExactlyInstanceOf(ExecutionException.class)
                .hasCauseExactlyInstanceOf(UnexpectedException.class);
    }
}
