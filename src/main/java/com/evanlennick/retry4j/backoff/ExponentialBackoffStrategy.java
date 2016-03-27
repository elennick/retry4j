package com.evanlennick.retry4j.backoff;

import java.time.Duration;

/**
 * AKA binary exponential backoff
 */
public class ExponentialBackoffStrategy implements BackoffStrategy {

    @Override
    public long getMillisToWait(int numberOfTriesFailed, Duration delayBetweenAttempts) {
        long exponentialMultiplier = (long) ((Math.pow(2L, numberOfTriesFailed) - 1L) / 2L);
        long millisToWait = exponentialMultiplier * delayBetweenAttempts.toMillis();
        return millisToWait;
    }
}
