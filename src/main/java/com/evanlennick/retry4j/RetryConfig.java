package com.evanlennick.retry4j;

import com.evanlennick.retry4j.backoff.BackoffStrategy;
import com.evanlennick.retry4j.backoff.ExponentialBackoffStrategy;
import com.evanlennick.retry4j.backoff.FibonacciBackoffStrategy;
import com.evanlennick.retry4j.backoff.FixedBackoffStrategy;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

public class RetryConfig {

    private Boolean retryOnAnyException = false;
    private Set<Class<? extends Exception>> retryOnSpecificExceptions = new HashSet<Class<? extends Exception>>();
    private Integer maxNumberOfTries;
    private Duration delayBetweenRetries;
    private BackoffStrategy backoffStrategy;

    public Boolean isRetryOnAnyException() {
        return retryOnAnyException;
    }

    public void setRetryOnAnyException(boolean retryOnAnyException) {
        this.retryOnAnyException = retryOnAnyException;
    }

    public Set<Class<? extends Exception>> getRetryOnSpecificExceptions() {
        return retryOnSpecificExceptions;
    }

    public void setRetryOnSpecificExceptions(Set<Class<? extends Exception>> retryOnSpecificExceptions) {
        this.retryOnSpecificExceptions = retryOnSpecificExceptions;
    }

    public Integer getMaxNumberOfTries() {
        return maxNumberOfTries;
    }

    public void setMaxNumberOfTries(int maxNumberOfTries) {
        if (maxNumberOfTries < 0) {
            throw new IllegalArgumentException("Must be non-negative number.");
        }

        this.maxNumberOfTries = maxNumberOfTries;
    }

    public Duration getDelayBetweenRetries() {
        return delayBetweenRetries;
    }

    public void setDelayBetweenRetries(Duration delayBetweenRetries) {
        if (delayBetweenRetries.isNegative()) {
            throw new IllegalArgumentException("Must be non-negative Duration.");
        }

        this.delayBetweenRetries = delayBetweenRetries;
    }

    public BackoffStrategy getBackoffStrategy() {
        return backoffStrategy;
    }

    public void setBackoffStrategy(BackoffStrategy backoffStrategy) {
        this.backoffStrategy = backoffStrategy;
    }

    public static RetryConfig simpleFixedConfig() {
        RetryConfig config = new RetryConfig();
        config.setRetryOnAnyException(true);
        config.setRetryOnSpecificExceptions(new HashSet<>());
        config.setMaxNumberOfTries(5);
        config.setDelayBetweenRetries(Duration.of(10, ChronoUnit.SECONDS));
        config.setBackoffStrategy(new FixedBackoffStrategy());
        return config;
    }

    public static RetryConfig simpleExponentialConfig() {
        RetryConfig config = new RetryConfig();
        config.setRetryOnAnyException(true);
        config.setRetryOnSpecificExceptions(new HashSet<>());
        config.setMaxNumberOfTries(5);
        config.setDelayBetweenRetries(Duration.of(5, ChronoUnit.SECONDS));
        config.setBackoffStrategy(new ExponentialBackoffStrategy());
        return config;
    }

    public static RetryConfig simpleFibonacciConfig() {
        RetryConfig config = new RetryConfig();
        config.setRetryOnAnyException(true);
        config.setRetryOnSpecificExceptions(new HashSet<>());
        config.setMaxNumberOfTries(7);
        config.setDelayBetweenRetries(Duration.of(7, ChronoUnit.SECONDS));
        config.setBackoffStrategy(new FibonacciBackoffStrategy());
        return config;
    }

    @Override
    public String toString() {
        return "RetryConfig{" +
                "retryOnAnyException=" + retryOnAnyException +
                ", retryOnSpecificExceptions=" + retryOnSpecificExceptions +
                ", maxNumberOfTries=" + maxNumberOfTries +
                ", delayBetweenRetries=" + delayBetweenRetries +
                ", backoffStrategy=" + backoffStrategy +
                '}';
    }
}
