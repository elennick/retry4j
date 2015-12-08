package com.evanlennick.retry4j;

import com.evanlennick.retry4j.backoff.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class RetryConfigBuilderTest_ValidationDisabledTest {

    private RetryConfigBuilder retryConfigBuilder;

    @BeforeMethod
    public void setup() {
        retryConfigBuilder = RetryConfigBuilder.newConfig();
        retryConfigBuilder.setValidationEnabled(false);
    }

    @Test
    public void testSettingRetryOnAnyException() {
        RetryConfig config = retryConfigBuilder
                .retryOnAnyException()
                .build();

        assertThat(config.isRetryOnAnyException());
    }

    @Test
    public void testSettingRetryOnSpecificExceptions() {
        RetryConfig config = retryConfigBuilder
                .retryOnSpecificExceptions(IllegalArgumentException.class, UnsupportedOperationException.class)
                .build();

        assertThat(config.getRetryOnSpecificExceptions())
                .containsOnly(IllegalArgumentException.class, UnsupportedOperationException.class);
    }

    @Test
    public void testSettingMaxTries() {
        RetryConfig config = retryConfigBuilder
                .withMaxNumberOfTries(99)
                .build();

        assertThat(config.getMaxNumberOfTries()).isEqualTo(99);
    }

    @Test
    public void testSettingDurationBetweenTries_duration() {
        Duration duration = Duration.of(60, ChronoUnit.MINUTES);

        RetryConfig config = retryConfigBuilder
                .withDelayBetweenTries(duration)
                .build();

        assertThat(config.getDelayBetweenRetries()).isEqualTo(duration);
    }

    @Test
    public void testSettingDurationBetweenTries_seconds() {
        RetryConfig config = retryConfigBuilder
                .withDelayBetweenTries(5, ChronoUnit.SECONDS)
                .build();

        assertThat(config.getDelayBetweenRetries().toMillis()).isEqualTo(5000);
    }

    @Test
    public void testSettingDurationBetweenTries_millis() {
        RetryConfig config = retryConfigBuilder
                .withDelayBetweenTries(5000, ChronoUnit.MILLIS)
                .build();

        assertThat(config.getDelayBetweenRetries().toMillis()).isEqualTo(5000);
    }

    @Test
    public void testSettingBackoffStrategy_exponential() {
        RetryConfig config = retryConfigBuilder
                .withExponentialBackoff()
                .build();

        assertThat(config.getBackoffStrategy()).isInstanceOf(ExponentialBackoffStrategy.class);
    }

    @Test
    public void testSettingBackoffStrategy_fixed() {
        RetryConfig config = retryConfigBuilder
                .withFixedBackoff()
                .build();

        assertThat(config.getBackoffStrategy()).isInstanceOf(FixedBackoffStrategy.class);
    }

    @Test
    public void testSettingBackoffStrategy_fibonacci() {
        RetryConfig config = retryConfigBuilder
                .withFibonacciBackoff()
                .build();

        assertThat(config.getBackoffStrategy()).isInstanceOf(FibonacciBackoffStrategy.class);
    }

    @Test
    public void testSettingBackoffStrategy_noWait() {
        RetryConfig config = retryConfigBuilder
                .withNoWaitBackoff()
                .build();

        assertThat(config.getBackoffStrategy()).isInstanceOf(NoWaitBackoffStrategy.class);
    }

    @Test
    public void testSettingBackoffStrategy_random() {
        RetryConfig config = retryConfigBuilder
                .withRandomBackoff()
                .build();

        assertThat(config.getBackoffStrategy()).isInstanceOf(RandomBackoffStrategy.class);
    }

    @Test
    public void testSettingBackoffStrategy_randomExponential() {
        RetryConfig config = retryConfigBuilder
                .withRandomExponentialBackoff()
                .build();

        assertThat(config.getBackoffStrategy()).isInstanceOf(RandomExponentialBackoffStrategy.class);
    }

    @Test
    public void testSettingBackoffStrategy_manualSetting() {
        RetryConfig config = retryConfigBuilder
                .withBackoffStrategy(new TestBackoffStrategy())
                .build();

        assertThat(config.getBackoffStrategy()).isInstanceOf(TestBackoffStrategy.class);
    }

    private class TestBackoffStrategy implements BackoffStrategy {
        @Override
        public long getMillisToWait(int numberOfTriesFailed, Duration delayBetweenAttempts) {
            return 0;
        }
    }

}
