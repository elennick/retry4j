package com.evanlennick.retry4j;

import com.evanlennick.retry4j.backoff.*;

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

    public RetryConfigBuilder failOnAnyException() {
        config.setRetryOnAnyException(false);
        config.setRetryOnSpecificExceptions(new HashSet<>());
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

    public RetryConfigBuilder withDelayBetweenTries(Duration duration) {
        config.setDelayBetweenRetries(duration);
        return this;
    }

    public RetryConfigBuilder withDelayBetweenTries(int seconds) {
        config.setDelayBetweenRetries(Duration.of(seconds, ChronoUnit.SECONDS));
        return this;
    }

    public RetryConfigBuilder withDelayBetweenTries(long millis) {
        config.setDelayBetweenRetries(Duration.of(millis, ChronoUnit.MILLIS));
        return this;
    }

    public RetryConfigBuilder withBackoffStrategy(BackoffStrategy backoffStrategy) {
        config.setBackoffStrategy(backoffStrategy);
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

    public RetryConfigBuilder withNoWaitBackoff() {
        config.setBackoffStrategy(new NoWaitBackoffStrategy());
        return this;
    }

    public RetryConfigBuilder withRandomBackoff() {
        config.setBackoffStrategy(new RandomBackoffStrategy());
        return this;
    }

    public RetryConfigBuilder withRandomExponentialBackoff() {
        config.setBackoffStrategy(new RandomExponentialBackoffStrategy());
        return this;
    }

    public RetryConfig build() {
        return config;
    }
}
