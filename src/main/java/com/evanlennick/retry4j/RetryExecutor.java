package com.evanlennick.retry4j;

import com.evanlennick.retry4j.exception.CallFailureException;
import com.evanlennick.retry4j.exception.UnexpectedCallFailureException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class RetryExecutor {

    private RetryConfig config;

    public RetryExecutor() {
        this.config = RetryConfig.simpleFixedConfig();
    }

    public RetryExecutor(RetryConfig config) {
        this.config = config;
    }

    public RetryResults execute(Callable<Boolean> callable) throws CallFailureException, UnexpectedCallFailureException {
        long start = System.currentTimeMillis();

        int maxTries = config.getMaxNumberOfTries();
        long millisBetweenTries = config.getDelayBetweenRetries().toMillis();

        boolean success = false;
        int tries;
        for (tries = 0; tries < maxTries && !success; tries++) {
            success = tryCall(callable);

            if (!success) {
                sleep(millisBetweenTries, tries);
            }
        }

        long end = System.currentTimeMillis();
        long elapsed = end - start;

        RetryResults results = new RetryResults();
        results.setCallName(callable.toString());
        results.setTotalTries(tries);
        results.setTotalDurationElapsed(Duration.of(elapsed, ChronoUnit.MILLIS));
        results.setSucceeded(success);

        if (!success) {
            String failureMsg = String.format("Call '%s' failed after %d tries!", callable.toString(), maxTries);
            throw new CallFailureException(failureMsg, results);
        } else {
            return results;
        }
    }

    private boolean tryCall(Callable<Boolean> callable) throws UnexpectedCallFailureException {
        boolean success = false;
        try {
            success = callable.call();
        } catch (Exception e) {
            if (shouldThrowException(e)) {
                throw new UnexpectedCallFailureException(e);
            }
        }
        return success;
    }

    private void sleep(long millis, int tries) {
        Duration duration = Duration.of(millis, ChronoUnit.MILLIS);
        long millisToSleep = config.getBackoffStrategy().getMillisToWait(tries, duration);

        try {
            TimeUnit.MILLISECONDS.sleep(millisToSleep);
        } catch (InterruptedException ignored) {
        }
    }

    private boolean shouldThrowException(Exception e) {
        if (this.config.isRetryOnAnyException()) {
            return false;
        }

        for (Class<? extends Exception> exceptionInSet : this.config.getRetryOnSpecificExceptions()) {
            if (e.getClass().isAssignableFrom(exceptionInSet)) {
                return false;
            }
        }

        return true;
    }
}
