package com.evanlennick.retry4j.backoff;

import org.testng.annotations.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class BackoffStrategyTest {

    @Test
    public void verifyBackoffStrategy_noWait() {
        NoWaitBackoffStrategy backoffStrategy = new NoWaitBackoffStrategy();
        Duration durationToWait = backoffStrategy.getDurationToWait(5, Duration.ofSeconds(30));

        assertThat(durationToWait.toMillis()).isEqualTo(0);
    }

    @Test
    public void verifyBackoffStrategy_fibonacci() {
        int delayInMillis = 5000;

        FibonacciBackoffStrategy backoffStrategy = new FibonacciBackoffStrategy();
        Duration durationToWait = backoffStrategy.getDurationToWait(4, Duration.ofMillis(delayInMillis));
        int fifthFibNumber = backoffStrategy.getFibonacciNumbers().get(4);

        assertThat(durationToWait.toMillis()).isEqualTo(fifthFibNumber * delayInMillis);
    }

    @Test
    public void verifyBackoffStrategy_fibonacci_noArrayIndexOutOfBounds() {
        FibonacciBackoffStrategy backoffStrategy = new FibonacciBackoffStrategy();
        backoffStrategy.getDurationToWait(100, Duration.ofSeconds(1));
    }

    @Test
    public void verifyBackoffStrategy_fixed() {
        long millisBetweenTries = 100;

        FixedBackoffStrategy backoffStrategy = new FixedBackoffStrategy();
        Duration durationToWait
                = backoffStrategy.getDurationToWait(5, Duration.ofMillis(millisBetweenTries));

        assertThat(durationToWait.toMillis()).isEqualTo(millisBetweenTries);
    }

    @Test
    public void verifyBackoffStrategy_random() {
        RandomBackoffStrategy backoffStrategy = new RandomBackoffStrategy();
        Duration durationToWait = backoffStrategy.getDurationToWait(10, Duration.ofMillis(100));

        assertThat(durationToWait.toMillis() % 100).isZero();
    }

    @Test
    public void verifyBackoffStrategy_exponential() {
        ExponentialBackoffStrategy backoffStrategy = new ExponentialBackoffStrategy();
        Duration durationToWait = backoffStrategy.getDurationToWait(5, Duration.ofMillis(100));
        assertThat(durationToWait.toMillis()).isEqualTo(1600L);
    }

    @Test
    public void verifyBackoffStrategy_exponentialFirstRetry() {
        ExponentialBackoffStrategy backoffStrategy = new ExponentialBackoffStrategy();
        Duration durationToWait = backoffStrategy.getDurationToWait(1, Duration.ofMillis(100));
        assertThat(durationToWait.toMillis()).isEqualTo(100L);
    }

    @Test
    public void verifyBackoffStrategy_exponentialRemainsPositive() {
        ExponentialBackoffStrategy backoffStrategy = new ExponentialBackoffStrategy();
        Duration durationToWait = backoffStrategy.getDurationToWait(10000, Duration.ofMillis(100));
        assertThat(durationToWait.toMillis()).isPositive();
    }
}
