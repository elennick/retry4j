package com.evanlennick.retry4j;

import com.evanlennick.retry4j.backoff.BackoffStrategy;
import com.evanlennick.retry4j.backoff.ExponentialBackoffStrategy;
import com.evanlennick.retry4j.backoff.FibonacciBackoffStrategy;
import com.evanlennick.retry4j.backoff.FixedBackoffStrategy;

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
        config.setBackoffStrategy(new FixedBackoffStrategy());
        return this;
    }

    public RetryConfigBuilder withExponentialBackoff() {
        config.setBackoffStrategy(new ExponentialBackoffStrategy());
        return this;
    }

    public RetryConfigBuilder withFibonacciBackoff() {
        config.setBackoffStrategy(new FibonacciBackoffStrategy());
        return this;
    }

    public RetryConfigBuilder withBackoffStrategy(BackoffStrategy backoffStrategy) {
        config.setBackoffStrategy(backoffStrategy);
        return this;
    }

    public RetryConfig build() {
        return config;
    }
}
