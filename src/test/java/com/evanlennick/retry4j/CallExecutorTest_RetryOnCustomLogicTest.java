package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

public class CallExecutorTest_RetryOnCustomLogicTest {

    private static RetryConfig config;

    @BeforeClass
    public static void setup() {
        config = new RetryConfigBuilder()
                .retryOnCustomExceptionLogic(ex -> ex.getMessage().contains("should retry!"))
                .withFixedBackoff()
                .withDelayBetweenTries(Duration.ofMillis(1))
                .withMaxNumberOfTries(3)
                .build();
    }

    @Test
    public void verifyShouldRetry() {
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
    public void verifyShouldNotRetry() {
        new CallExecutor<>(config)
                .execute(() -> {
                    throw new RuntimeException("should NOT retry!");
                });
    }
}
