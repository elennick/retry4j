package com.evanlennick.retry4j.backoff;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class RandomBackoffStrategy implements BackoffStrategy {

    private int maxMultiplier;

    public RandomBackoffStrategy() {
        this.maxMultiplier = 10;
    }

    public RandomBackoffStrategy(int maxMultiplier) {
        this.maxMultiplier = maxMultiplier;
    }

    @Override
    public Duration getDurationToWait(int numberOfTriesFailed, Duration delayBetweenAttempts) {
        int i = ThreadLocalRandom.current().nextInt(0, maxMultiplier - 1);
        return Duration.ofMillis(i * delayBetweenAttempts.toMillis());
    }
}
