package com.evanlennick.retry4j.backoff;

import org.testng.annotations.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertTrue;

public class BackoffStrategyTest {

    @Test
    public void verifyBackoffStrategy_noWait() {
        NoWaitBackoffStrategy backoffStrategy = new NoWaitBackoffStrategy();
        long millisToWait = backoffStrategy.getMillisToWait(5, Duration.ofSeconds(30));

        assertThat(millisToWait).isZero(); //this backoff strategy should always be 0 regardless of whats passed in
    }

    @Test
    public void verifyBackoffStrategy_fibonacci() {
        int delayInMillis = 5000;

        FibonacciBackoffStrategy backoffStrategy = new FibonacciBackoffStrategy();
        long millisToWait = backoffStrategy.getMillisToWait(4, Duration.ofMillis(delayInMillis));
        int fifthFibNumber = backoffStrategy.getFibonacciNumbers().get(4);

        assertThat(millisToWait).isEqualTo(fifthFibNumber * delayInMillis);
    }

    @Test
    public void verifyBackoffStrategy_fibonacci_noArrayIndexOutOfBounds() {
        FibonacciBackoffStrategy backoffStrategy = new FibonacciBackoffStrategy();
        backoffStrategy.getMillisToWait(100, Duration.ofSeconds(1)); //really high number of attempts shouldnt throw an exception
    }

    @Test
    public void verifyBackoffStrategy_fixed() {
        long millisBetweenTries = 100;

        FixedBackoffStrategy backoffStrategy = new FixedBackoffStrategy();
        long millisToWait = backoffStrategy.getMillisToWait(5, Duration.ofMillis(millisBetweenTries));

        assertThat(millisToWait).isEqualTo(millisBetweenTries);
    }

    @Test
    public void verifyBackoffStrategy_random() {
        RandomBackoffStrategy backoffStrategy = new RandomBackoffStrategy();
        long millisToWait = backoffStrategy.getMillisToWait(10, Duration.ofMillis(100));

        assertThat(millisToWait % 100).isZero(); //todo write custom assertion for this
    }

    @Test
    public void verifyBackoffStrategy_exponential() {
        ExponentialBackoffStrategy backoffStrategy = new ExponentialBackoffStrategy();
        long millisToWait = backoffStrategy.getMillisToWait(5, Duration.ofMillis(100));
        assertThat(millisToWait).isEqualTo(1600L);
    }

    @Test
    public void verifyBackoffStrategy_exponentialFirstRetry() {
        ExponentialBackoffStrategy backoffStrategy = new ExponentialBackoffStrategy();
        long millisToWait = backoffStrategy.getMillisToWait(1, Duration.ofMillis(100));
        assertThat(millisToWait).isEqualTo(100L);
    }

    @Test
    public void verifyBackoffStrategy_exponentialRemainsPositive() {
        ExponentialBackoffStrategy backoffStrategy = new ExponentialBackoffStrategy();
        long millisToWait = backoffStrategy.getMillisToWait(10000, Duration.ofMillis(100));
        assertThat(millisToWait).isPositive();
    }
}
