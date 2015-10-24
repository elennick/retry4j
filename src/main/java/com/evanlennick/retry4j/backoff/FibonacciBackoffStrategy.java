package com.evanlennick.retry4j.backoff;

import java.time.Duration;

public class FibonacciBackoffStrategy implements BackoffStrategy {
    @Override
    public long getMillisToWait(int numberOfTriesFailed, Duration durationBetweenAttempts) {
        throw new UnsupportedOperationException("method not implemented!");
    }
}
