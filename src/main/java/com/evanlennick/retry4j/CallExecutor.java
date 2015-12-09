package com.evanlennick.retry4j;

import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.evanlennick.retry4j.listener.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class CallExecutor<T> {

    private RetryConfig config;

    private AfterFailedTryListener afterFailedTryListener;

    private BeforeNextTryListener beforeNextTryListener;

    private OnFailureListener onFailureListener;

    private OnSuccessListener onSuccessListener;

    private ExecutorService executorService;

    private Exception lastKnownExceptionThatCausedRetry;

    private CallResults<T> results = new CallResults<>();

    public CallExecutor() {
        this(new RetryConfigBuilder().fixedBackoff5Tries10Sec().build());
    }

    public CallExecutor(RetryConfig config) {
        this.config = config;
    }

    public CallResults<T> execute(Callable<T> callable, RetryConfig config) {
        this.config = config;
        return execute(callable);
    }

    public CallResults<T> execute(Callable<T> callable) throws RetriesExhaustedException, UnexpectedException {
        long start = System.currentTimeMillis();
        results.setStartTime(start);

        int maxTries = config.getMaxNumberOfTries();
        long millisBetweenTries = config.getDelayBetweenRetries().toMillis();
        this.results.setCallName(callable.toString());

        Optional<T> result = Optional.empty();
        int tries;

        for (tries = 0; tries < maxTries && !result.isPresent(); tries++) {
            result = tryCall(callable);

            if (!result.isPresent()) {
                handleRetry(millisBetweenTries, tries + 1);
            }
        }

        refreshRetryResults(result.isPresent(), tries);
        results.setEndTime(System.currentTimeMillis());

        postExecutionCleanup(callable, maxTries, result);

        return results;
    }

    private void postExecutionCleanup(Callable<T> callable, int maxTries, Optional<T> callResult) {
        if (!callResult.isPresent()) {
            String failureMsg = String.format("Call '%s' failed after %d tries!", callable.toString(), maxTries);
            if (null != onFailureListener) {
                onFailureListener.onFailure(results);
            } else {
                throw new RetriesExhaustedException(failureMsg, results);
            }
        } else {
            results.setResult(callResult.get());
            if (null != onSuccessListener) {
                onSuccessListener.onSuccess(results);
            }
        }
    }

    private Optional<T> tryCall(Callable<T> callable) throws UnexpectedException {
        try {
            T result = callable.call();
            return Optional.of(result);
        } catch (Exception e) {
            if (shouldThrowException(e)) {
                throw new UnexpectedException(e);
            } else {
                lastKnownExceptionThatCausedRetry = e;
                return Optional.empty();
            }
        }
    }

    private void handleRetry(long millisBetweenTries, int tries) {
        refreshRetryResults(false, tries);

        if (null != afterFailedTryListener) {
            afterFailedTryListener.immediatelyAfterFailedTry(results);
        }

        sleep(millisBetweenTries, tries);

        if (null != beforeNextTryListener) {
            beforeNextTryListener.immediatelyBeforeNextTry(results);
        }
    }

    private void refreshRetryResults(boolean success, int tries) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - results.getStartTime();

        results.setTotalTries(tries);
        results.setTotalElapsedDuration(Duration.of(elapsed, ChronoUnit.MILLIS));
        results.setSuccessful(success);
        results.setLastExceptionThatCausedRetry(lastKnownExceptionThatCausedRetry);
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

    public void registerRetryListener(RetryListener listener) {
        if (listener instanceof AfterFailedTryListener) {
            this.afterFailedTryListener = (AfterFailedTryListener) listener;
        } else if (listener instanceof BeforeNextTryListener) {
            this.beforeNextTryListener = (BeforeNextTryListener) listener;
        } else if (listener instanceof OnSuccessListener) {
            this.onSuccessListener = (OnSuccessListener) listener;
        } else if (listener instanceof OnFailureListener) {
            this.onFailureListener = (OnFailureListener) listener;
        } else {
            throw new IllegalArgumentException("Tried to register an unrecognized RetryListener!");
        }
    }

    public RetryConfig getConfig() {
        return config;
    }
}
