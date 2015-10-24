package com.evanlennick.retry4j.backoff;

import java.time.Duration;

public class FixedBackoffStrategy implements BackoffStrategy {
    @Override
    public long getMillisToWait(int numberOfTriesFailed, Duration durationBetweenAttempts) {
        return durationBetweenAttempts.toMillis();
    }
}
