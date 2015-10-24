package com.evanlennick.retry4j.backoff;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class FibonacciBackoffStrategy implements BackoffStrategy {

    List<Integer> fibonacciNumbers;

    public FibonacciBackoffStrategy() {
        fibonacciNumbers = new ArrayList<>();

        fibonacciNumbers.add(0);
        fibonacciNumbers.add(1);

        for(int i = 0; i < 20; i++) {
            int nextFibNum = fibonacciNumbers.get(i) + fibonacciNumbers.get(i + 1);
            fibonacciNumbers.add(nextFibNum);
        }
    }

    @Override
    public long getMillisToWait(int numberOfTriesFailed, Duration delayBetweenAttempts) {
        int fibNumber = fibonacciNumbers.get(numberOfTriesFailed);
        return delayBetweenAttempts.toMillis() * fibNumber;
    }
}
