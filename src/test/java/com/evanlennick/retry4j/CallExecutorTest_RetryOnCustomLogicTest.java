package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import org.junit.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

public class CallExecutorTest_RetryOnCustomLogicTest {

    @Test
    public void verifyShouldRetryOnExceptionMessage() {
        RetryConfig config = new RetryConfigBuilder()
                .retryOnCustomExceptionLogic(ex -> ex.getMessage().contains("should retry!"))
                .withFixedBackoff()
                .withDelayBetweenTries(Duration.ofMillis(1))
                .withMaxNumberOfTries(3)
                .build();

        try {
            new CallExecutor<>(config)
                    .execute(() -> {
                        throw new RuntimeException("should retry!");
                    });
            fail();
        } catch (RetriesExhaustedException e) {
            assertThat(e.getStatus().getTotalTries()).isEqualTo(3);
        }
    }

    @Test(expected = UnexpectedException.class)
    public void verifyShouldNotRetryOnExceptionMessage() {
        RetryConfig config = new RetryConfigBuilder()
                .retryOnCustomExceptionLogic(ex -> ex.getMessage().contains("should retry!"))
                .withFixedBackoff()
                .withDelayBetweenTries(Duration.ofMillis(1))
                .withMaxNumberOfTries(3)
                .build();

        new CallExecutor<>(config)
                .execute(() -> {
                    throw new RuntimeException("should NOT retry!");
                });
    }

    @Test
    public void verifyShouldRetryOnCustomException() {
        RetryConfig config = new RetryConfigBuilder()
                .retryOnCustomExceptionLogic(ex -> ((CustomTestException) ex).getSomeValue() > 0)
                .withFixedBackoff()
                .withDelayBetweenTries(Duration.ofMillis(1))
                .withMaxNumberOfTries(3)
                .build();

        try {
            new CallExecutor<>(config)
                    .execute(() -> {
                        throw new CustomTestException("should retry!", 100);
                    });
            fail();
        } catch (RetriesExhaustedException e) {
            assertThat(e.getStatus().getTotalTries()).isEqualTo(3);
        }
    }

    @Test(expected = UnexpectedException.class)
    public void verifyShouldNotRetryOnCustomException() {
        RetryConfig config = new RetryConfigBuilder()
                .retryOnCustomExceptionLogic(ex -> ((CustomTestException) ex).getSomeValue() > 0)
                .withFixedBackoff()
                .withDelayBetweenTries(Duration.ofMillis(1))
                .withMaxNumberOfTries(3)
                .build();

        new CallExecutor<>(config)
                .execute(() -> {
                    throw new CustomTestException("test message", -100);
                });
    }

    private class CustomTestException extends RuntimeException {

        private int someValue;

        public CustomTestException(String message, int someValue) {
            super(message);
            this.someValue = someValue;
        }

        public int getSomeValue() {
            return someValue;
        }
    }
}
