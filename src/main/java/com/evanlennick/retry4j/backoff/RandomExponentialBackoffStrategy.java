package com.evanlennick.retry4j.backoff;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class RandomExponentialBackoffStrategy implements BackoffStrategy {

    private RandomBackoffStrategy randomBackoffStrategy;

    private ExponentialBackoffStrategy exponentialBackoffStrategy;

    public RandomExponentialBackoffStrategy() {
        this.randomBackoffStrategy = new RandomBackoffStrategy(10);
        this.exponentialBackoffStrategy = new ExponentialBackoffStrategy();
    }

    @Override
    public long getMillisToWait(int numberOfTriesFailed, Duration delayBetweenAttempts) {
        long millisToWaitFromExpBackoff
            = exponentialBackoffStrategy.getMillisToWait(numberOfTriesFailed, delayBetweenAttempts);

        long millisToWait
            = randomBackoffStrategy.getMillisToWait(numberOfTriesFailed,
            Duration.of(millisToWaitFromExpBackoff, ChronoUnit.MILLIS));

        return millisToWait;
    }
}
