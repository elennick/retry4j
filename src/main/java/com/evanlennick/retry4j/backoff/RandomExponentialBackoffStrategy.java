package com.evanlennick.retry4j.backoff;

import java.time.Duration;

public class RandomExponentialBackoffStrategy implements BackoffStrategy {

    private RandomBackoffStrategy randomBackoffStrategy;

    private ExponentialBackoffStrategy exponentialBackoffStrategy;

    public RandomExponentialBackoffStrategy() {
        this.randomBackoffStrategy = new RandomBackoffStrategy(10);
        this.exponentialBackoffStrategy = new ExponentialBackoffStrategy();
    }

    @Override
    public Duration getDurationToWait(int numberOfTriesFailed, Duration delayBetweenAttempts) {
        Duration durationWaitFromExpBackoff
                = exponentialBackoffStrategy.getDurationToWait(numberOfTriesFailed, delayBetweenAttempts);

        return randomBackoffStrategy.getDurationToWait(numberOfTriesFailed, durationWaitFromExpBackoff);
    }
}
