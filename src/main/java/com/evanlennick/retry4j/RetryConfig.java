package com.evanlennick.retry4j;

import com.evanlennick.retry4j.backoff.BackoffStrategy;
import com.evanlennick.retry4j.backoff.FixedBackoffStrategy;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

public class RetryConfig {

    private boolean retryOnAnyException = false;
    private Set<Class<? extends Exception>> retryOnSpecificExceptions = new HashSet<Class<? extends Exception>>();
    private int maxNumberOfTries = 1;
    private Duration delayBetweenRetries = Duration.of(5, ChronoUnit.SECONDS);
    private BackoffStrategy backoffStrategy = new FixedBackoffStrategy();

    public boolean isRetryOnAnyException() {
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

    public int getMaxNumberOfTries() {
        return maxNumberOfTries;
    }

    public void setMaxNumberOfTries(int maxNumberOfTries) {
        this.maxNumberOfTries = maxNumberOfTries;
    }

    public Duration getDelayBetweenRetries() {
        return delayBetweenRetries;
    }

    public void setDelayBetweenRetries(Duration delayBetweenRetries) {
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
        config.retryOnAnyException = true;
        config.retryOnSpecificExceptions = new HashSet<>();
        config.maxNumberOfTries = 3;
        config.delayBetweenRetries = Duration.of(15, ChronoUnit.SECONDS);
        config.backoffStrategy = new FixedBackoffStrategy();
        return config;
    }

    public static RetryConfig simpleExponentialConfig() {
        RetryConfig config = new RetryConfig();
        config.retryOnAnyException = true;
        config.retryOnSpecificExceptions = new HashSet<>();
        config.maxNumberOfTries = 5;
        config.delayBetweenRetries = Duration.of(5, ChronoUnit.SECONDS);
        config.backoffStrategy = new FixedBackoffStrategy();
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
