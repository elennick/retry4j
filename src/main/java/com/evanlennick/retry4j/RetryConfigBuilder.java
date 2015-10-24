package com.evanlennick.retry4j;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RetryConfigBuilder {

    private RetryConfig config;

    public RetryConfigBuilder() {
        config = new RetryConfig();
    }

    public RetryConfigBuilder retryOnAnyException() {
        config.setRetryOnAnyException(true);
        return this;
    }

    @SafeVarargs
    public final RetryConfigBuilder retryOnSpecificExceptions(Class<? extends Exception>... exceptions) {
        Set<Class<? extends Exception>> setOfExceptions = new HashSet<>(Arrays.asList(exceptions));
        config.setRetryOnSpecificExceptions(setOfExceptions);
        return this;
    }

    public RetryConfigBuilder withMaxNumberOfTries(int max) {
        config.setMaxNumberOfTries(max);
        return this;
    }

    public RetryConfigBuilder withDurationBetweenTries(Duration duration) {
        config.setDurationBetweenRetries(duration);
        return this;
    }

    public RetryConfigBuilder withDurationBetweenTries(int seconds) {
        config.setDurationBetweenRetries(Duration.of(seconds, ChronoUnit.SECONDS));
        return this;
    }

    public RetryConfigBuilder withDurationBetweenTries(long millis) {
        config.setDurationBetweenRetries(Duration.of(millis, ChronoUnit.MILLIS));
        return this;
    }

    public RetryConfigBuilder withFixedBackoff() {
        config.setBackoffStrategy(BackoffStrategy.FIXED);
        return this;
    }

    public RetryConfigBuilder withExponentialBackoff() {
        config.setBackoffStrategy(BackoffStrategy.EXPONENTIAL);
        return this;
    }

    public RetryConfig build() {
        return config;
    }
}
