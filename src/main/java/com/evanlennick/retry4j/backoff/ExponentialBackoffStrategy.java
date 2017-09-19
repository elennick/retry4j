package com.evanlennick.retry4j.backoff;

import java.time.Duration;

/**
 * AKA binary exponential backoff
 */
public class ExponentialBackoffStrategy implements BackoffStrategy {

    @Override
    public long getMillisToWait(int numberOfTriesFailed, Duration delayBetweenAttempts) {
        double exponentialMultiplier = Math.pow(2.0, numberOfTriesFailed - 1);
        double result = exponentialMultiplier * delayBetweenAttempts.toMillis();
        return (long) Math.min(result, Long.MAX_VALUE);
    }
}
