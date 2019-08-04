package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import org.testng.annotations.Test;

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
            new CallExecutorBuilder().config(config).build()
                    .execute(() -> {
                        throw new RuntimeException("should retry!");
                    });
            fail();
        } catch (RetriesExhaustedException e) {
            assertThat(e.getStatus().getTotalTries()).isEqualTo(3);
        }
    }

    @Test(expectedExceptions = UnexpectedException.class)
    public void verifyShouldNotRetryOnExceptionMessage() {
        RetryConfig config = new RetryConfigBuilder()
                .retryOnCustomExceptionLogic(ex -> ex.getMessage().contains("should retry!"))
                .withFixedBackoff()
                .withDelayBetweenTries(Duration.ofMillis(1))
                .withMaxNumberOfTries(3)
                .build();

        new CallExecutorBuilder().config(config)
                .build()
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
            new CallExecutorBuilder().config(config).build()
                    .execute(() -> {
                        throw new CustomTestException("should retry!", 100);
                    });
            fail();
        } catch (RetriesExhaustedException e) {
            assertThat(e.getStatus().getTotalTries()).isEqualTo(3);
        }
    }

    @Test(expectedExceptions = UnexpectedException.class)
    public void verifyShouldNotRetryOnCustomException() {
        RetryConfig config = new RetryConfigBuilder()
                .retryOnCustomExceptionLogic(ex -> ((CustomTestException) ex).getSomeValue() > 0)
                .withFixedBackoff()
                .withDelayBetweenTries(Duration.ofMillis(1))
                .withMaxNumberOfTries(3)
                .build();

        new CallExecutorBuilder().config(config).build()
                .execute(() -> {
                    throw new CustomTestException("test message", -100);
                });
    }

}
