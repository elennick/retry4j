package com.evanlennick.retry4j;

import org.testng.annotations.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class RetryConfigBuilderTest {

    @Test
    public void testSettingRetryOnAnyException() {
        RetryConfig config = new RetryConfigBuilder()
                .retryOnAnyException()
                .build();

        assertThat(config.isRetryOnAnyException());
    }

    @Test
    public void testSettingRetryOnSpecificExceptions() {
        RetryConfig config = new RetryConfigBuilder()
                .retryOnSpecificExceptions(IllegalArgumentException.class, UnsupportedOperationException.class)
                .build();

        assertThat(config.getRetryOnSpecificExceptions())
                .containsOnly(IllegalArgumentException.class, UnsupportedOperationException.class);
    }

    @Test
    public void testSettingMaxTries() {
        RetryConfig config = new RetryConfigBuilder()
                .withMaxNumberOfTries(99)
                .build();

        assertThat(config.getMaxNumberOfTries()).isEqualTo(99);
    }

    @Test
    public void testSettingDurationBetweenTries() {
        Duration duration = Duration.of(60, ChronoUnit.MINUTES);

        RetryConfig config = new RetryConfigBuilder()
                .withDurationBetweenTries(duration)
                .build();

        assertThat(config.getDurationBetweenRetries()).isEqualTo(duration);
    }

    @Test
    public void testSettingBackoffStrategy_exponential() {
        RetryConfig config = new RetryConfigBuilder()
                .withExponentialBackoff()
                .build();

        assertThat(config.getBackoffStrategy()).isEqualTo(BackoffStrategy.EXPONENTIAL);
    }

    @Test
    public void testSettingBackoffStrategy_fixed() {
        RetryConfig config = new RetryConfigBuilder()
                .withFixedBackoff()
                .build();

        assertThat(config.getBackoffStrategy()).isEqualTo(BackoffStrategy.FIXED);
    }

}
