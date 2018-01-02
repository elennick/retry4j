package com.evanlennick.retry4j.backoff;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class FibonacciBackoffStrategy implements BackoffStrategy {

    private List<Integer> fibonacciNumbers;

    public static final int MAX_NUM_OF_FIB_NUMBERS = 25;

    public FibonacciBackoffStrategy() {
        fibonacciNumbers = new ArrayList<>();

        fibonacciNumbers.add(0);
        fibonacciNumbers.add(1);

        for (int i = 0; i < MAX_NUM_OF_FIB_NUMBERS; i++) {
            int nextFibNum = fibonacciNumbers.get(i) + fibonacciNumbers.get(i + 1);
            fibonacciNumbers.add(nextFibNum);
        }
    }

    @Override
    public Duration getDurationToWait(int numberOfTriesFailed, Duration delayBetweenAttempts) {
        int fibNumber;
        try {
            fibNumber = fibonacciNumbers.get(numberOfTriesFailed);
        } catch (IndexOutOfBoundsException e) {
            fibNumber = fibonacciNumbers.get(MAX_NUM_OF_FIB_NUMBERS - 1);
        }
        return Duration.ofMillis(delayBetweenAttempts.toMillis() * fibNumber);
    }

    public List<Integer> getFibonacciNumbers() {
        return fibonacciNumbers;
    }
}
