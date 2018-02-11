package com.evanlennick.retry4j.backoff;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.exception.InvalidRetryConfigException;

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

    @Override
    public void validateConfig(RetryConfig config) {
        if (null == config.getDelayBetweenRetries()) {
            throw new InvalidRetryConfigException("Retry config must specify the delay between retries!");
        }
    }

}
