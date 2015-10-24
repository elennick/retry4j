package com.evanlennick.retry4j;

import com.evanlennick.retry4j.backoff.ExponentialBackoffStrategy;
import com.evanlennick.retry4j.backoff.FibonacciBackoffStrategy;
import com.evanlennick.retry4j.backoff.FixedBackoffStrategy;
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
    public void testSettingDurationBetweenTries_duration() {
        Duration duration = Duration.of(60, ChronoUnit.MINUTES);

        RetryConfig config = new RetryConfigBuilder()
                .withDurationBetweenTries(duration)
                .build();

        assertThat(config.getDelayBetweenRetries()).isEqualTo(duration);
    }

    @Test
    public void testSettingDurationBetweenTries_seconds() {
        RetryConfig config = new RetryConfigBuilder()
                .withDurationBetweenTries(5)
                .build();

        assertThat(config.getDelayBetweenRetries().toMillis()).isEqualTo(5000);
    }

    @Test
    public void testSettingDurationBetweenTries_millis() {
        RetryConfig config = new RetryConfigBuilder()
                .withDurationBetweenTries(5000L)
                .build();

        assertThat(config.getDelayBetweenRetries().toMillis()).isEqualTo(5000);
    }

    @Test
    public void testSettingBackoffStrategy_exponential() {
        RetryConfig config = new RetryConfigBuilder()
                .withExponentialBackoff()
                .build();

        assertThat(config.getBackoffStrategy()).isInstanceOf(ExponentialBackoffStrategy.class);
    }

    @Test
    public void testSettingBackoffStrategy_fixed() {
        RetryConfig config = new RetryConfigBuilder()
                .withFixedBackoff()
                .build();

        assertThat(config.getBackoffStrategy()).isInstanceOf(FixedBackoffStrategy.class);
    }

    @Test
    public void testSettingBackoffStrategy_fibonacci() {
        RetryConfig config = new RetryConfigBuilder()
                .withFibonacciBackoff()
                .build();

        assertThat(config.getBackoffStrategy()).isInstanceOf(FibonacciBackoffStrategy.class);
    }

}
