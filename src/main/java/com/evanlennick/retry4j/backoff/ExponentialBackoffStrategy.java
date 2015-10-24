package com.evanlennick.retry4j.backoff;

import java.time.Duration;

public class ExponentialBackoffStrategy implements BackoffStrategy {
    @Override
    public long getMillisToWait(int numberOfTriesFailed, Duration delayBetweenAttempts) {
        return (long)(((Math.pow(2, numberOfTriesFailed) - 1) / 2) * delayBetweenAttempts.toMillis());
    }
}
