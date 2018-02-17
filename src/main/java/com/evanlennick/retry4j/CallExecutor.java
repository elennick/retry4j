package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.evanlennick.retry4j.listener.RetryListener;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation that does a single, synchrnous retry in the same thread that it is called from.
 *
 * @param <T> The type that is returned by the Callable (eg: Boolean, Void, Object, etc)
 */
@Slf4j
public class CallExecutor<T> implements RetryExecutor<T, Status<T>> {

    private final RetryConfig config;

    private final RetryListener afterFailedTryListener;

    private final RetryListener beforeNextTryListener;

    private final RetryListener onFailureListener;

    private final RetryListener onSuccessListener;

    private final RetryListener onCompletionListener;

    private Exception lastKnownExceptionThatCausedRetry;

    private Status<T> status = new Status<>();

    @Builder
    private CallExecutor(RetryConfig withConfig,
                         RetryListener afterFailedTry,
                         RetryListener beforeNextTry,
                         RetryListener onFailure,
                         RetryListener onSuccess,
                         RetryListener onCompletion) {
        this.config = withConfig;
        this.afterFailedTryListener = afterFailedTry;
        this.beforeNextTryListener = beforeNextTry;
        this.onFailureListener = onFailure;
        this.onSuccessListener = onSuccess;
        this.onCompletionListener = onCompletion;
        this.status.setId(UUID.randomUUID().toString());
    }

    @Override
    public Status<T> execute(Callable<T> callable) {
        return execute(callable, null);
    }

    @Override
    public Status<T> execute(Callable<T> callable, String callName) {
        log.trace("Starting retry4j execution with callable {}", config, callable);
        log.debug("Starting retry4j execution with executor state {}", this);

        long start = System.currentTimeMillis();
        status.setStartTime(start);

        int maxTries = config.getMaxNumberOfTries();
        long millisBetweenTries = config.getDelayBetweenRetries().toMillis();
        this.status.setCallName(callName);

        AttemptStatus<T> attemptStatus = new AttemptStatus<>();
        attemptStatus.setSuccessful(false);

        int tries;

        try {
            for (tries = 0; tries < maxTries && !attemptStatus.wasSuccessful(); tries++) {
                log.trace("Retry4j executing callable {}", callable);
                attemptStatus = tryCall(callable);

                if (!attemptStatus.wasSuccessful()) {
                    handleRetry(millisBetweenTries, tries + 1);
                }

                log.trace("Retry4j retrying for time number {}", tries);
            }

            refreshRetryStatus(attemptStatus.wasSuccessful(), tries);
            status.setEndTime(System.currentTimeMillis());

            postExecutionCleanup(callable, maxTries, attemptStatus);

            log.debug("Finished retry4j execution in {} ms", status.getTotalElapsedDuration().toMillis());
            log.trace("Finished retry4j execution with executor state {}", this);
        } finally {
            if (null != onCompletionListener) {
                onCompletionListener.onEvent(status);
            }
        }

        return status;
    }

    private void postExecutionCleanup(Callable<T> callable, int maxTries, AttemptStatus<T> attemptStatus) {
        if (!attemptStatus.wasSuccessful()) {
            String failureMsg = String.format("Call '%s' failed after %d tries!", callable.toString(), maxTries);
            if (null != onFailureListener) {
                onFailureListener.onEvent(status);
            } else {
                log.trace("Throwing retries exhausted exception");
                throw new RetriesExhaustedException(failureMsg, lastKnownExceptionThatCausedRetry, status);
            }
        } else {
            status.setResult(attemptStatus.getResult());
            if (null != onSuccessListener) {
                onSuccessListener.onEvent(status);
            }
        }
    }

    private AttemptStatus<T> tryCall(Callable<T> callable) throws UnexpectedException {
        AttemptStatus attemptStatus = new AttemptStatus();

        try {
            T callResult = callable.call();

            boolean shouldRetryOnThisResult
                    = config.shouldRetryOnValue() && callResult.equals(config.getValueToRetryOn());
            if (shouldRetryOnThisResult) {
                attemptStatus.setSuccessful(false);
            } else {
                attemptStatus.setResult(callResult);
                attemptStatus.setSuccessful(true);
            }
        } catch (Exception e) {
            if (shouldThrowException(e)) {
                log.trace("Throwing expected exception {}", e);
                throw new UnexpectedException("Unexpected exception thrown during retry execution!", e);
            } else {
                lastKnownExceptionThatCausedRetry = e;
                attemptStatus.setSuccessful(false);
            }
        }

        return attemptStatus;
    }

    private void handleRetry(long millisBetweenTries, int tries) {
        refreshRetryStatus(false, tries);

        if (null != afterFailedTryListener) {
            afterFailedTryListener.onEvent(status);
        }

        sleep(millisBetweenTries, tries);

        if (null != beforeNextTryListener) {
            beforeNextTryListener.onEvent(status);
        }
    }

    private void refreshRetryStatus(boolean success, int tries) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - status.getStartTime();

        status.setTotalTries(tries);
        status.setTotalElapsedDuration(Duration.of(elapsed, ChronoUnit.MILLIS));
        status.setSuccessful(success);
        status.setLastExceptionThatCausedRetry(lastKnownExceptionThatCausedRetry);
    }

    private void sleep(long millis, int tries) {
        Duration duration = Duration.of(millis, ChronoUnit.MILLIS);
        long millisToSleep = config.getBackoffStrategy().getDurationToWait(tries, duration).toMillis();

        log.trace("Retry4j executor sleeping for {} ms", millisToSleep);
        try {
            TimeUnit.MILLISECONDS.sleep(millisToSleep);
        } catch (InterruptedException ignored) {}
    }

    private boolean shouldThrowException(Exception e) {
        //config says to always retry
        if (this.config.isRetryOnAnyException()) {
            return false;
        }

        //config says to retry only on specific exceptions
        for (Class<? extends Exception> exceptionInSet : this.config.getRetryOnSpecificExceptions()) {
            if (exceptionInSet.isAssignableFrom(e.getClass())) {
                return false;
            }
        }

        //config says to retry on all except specific exceptions
        for (Class<? extends Exception> exceptionInSet : this.config.getRetryOnAnyExceptionExcluding()) {
            if (!exceptionInSet.isAssignableFrom(e.getClass())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallExecutor{");
        sb.append("config=").append(config);
        sb.append(", afterFailedTryListener=").append(afterFailedTryListener);
        sb.append(", beforeNextTryListener=").append(beforeNextTryListener);
        sb.append(", onFailureListener=").append(onFailureListener);
        sb.append(", onSuccessListener=").append(onSuccessListener);
        sb.append(", lastKnownExceptionThatCausedRetry=").append(lastKnownExceptionThatCausedRetry);
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }
}
