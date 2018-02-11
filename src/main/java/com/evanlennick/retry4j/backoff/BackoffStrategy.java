package com.evanlennick.retry4j.backoff;

import com.evanlennick.retry4j.config.RetryConfig;

import java.time.Duration;

public interface BackoffStrategy {

    Duration getDurationToWait(int numberOfTriesFailed, Duration delayBetweenAttempts);

    default void validateConfig(RetryConfig config) {}

}
