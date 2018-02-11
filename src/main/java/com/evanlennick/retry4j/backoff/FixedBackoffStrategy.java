package com.evanlennick.retry4j.backoff;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.exception.InvalidRetryConfigException;

import java.time.Duration;

public class FixedBackoffStrategy implements BackoffStrategy {

    @Override
    public Duration getDurationToWait(int numberOfTriesFailed, Duration delayBetweenAttempts) {
        return delayBetweenAttempts;
    }

    @Override
    public void validateConfig(RetryConfig config) {
        if (null == config.getDelayBetweenRetries()) {
            throw new InvalidRetryConfigException("Retry config must specify the delay between retries!");
        }
    }

}
