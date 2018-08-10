package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.evanlennick.retry4j.listener.RetryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation that does a single, synchronous retry in the same thread that it is called from.
 *
 * @param <T> The type that is returned by the Callable (eg: Boolean, Void, Object, etc)
 */
public class CallExecutor<T> implements RetryExecutor<T, Status<T>> {

    private Logger logger = LoggerFactory.getLogger(CallExecutor.class);

    private RetryConfig config;

    private RetryListener<T> afterFailedTryListener;

    private RetryListener<T> beforeNextTryListener;

    private RetryListener<T> onFailureListener;

    private RetryListener<T> onSuccessListener;

    private RetryListener<T> onCompletionListener;

    private Exception lastKnownExceptionThatCausedRetry;

    private Status<T> status = new Status<>();

    public CallExecutor() {
        this(new RetryConfigBuilder().fixedBackoff5Tries10Sec().build());
    }

    public CallExecutor(RetryConfig config) {
        this.config = config;
        this.status.setId(UUID.randomUUID().toString());
    }

    @Override
    public Status<T> execute(Callable<T> callable) {
        return execute(callable, null);
    }

    @Override
    public Status<T> execute(Callable<T> callable, String callName) {
        logger.trace("Starting retry4j execution with callable {}", config, callable);
        logger.debug("Starting retry4j execution with executor state {}", this);

        long start = System.currentTimeMillis();
        status.setStartTime(start);

        int maxTries = config.getMaxNumberOfTries();
        long millisBetweenTries = config.getDelayBetweenRetries() != null
                ? config.getDelayBetweenRetries().toMillis() : 0L;
        this.status.setCallName(callName);

        AttemptStatus<T> attemptStatus = new AttemptStatus<>();
        attemptStatus.setSuccessful(false);

        int tries;

        try {
            for (tries = 0; tries < maxTries && !attemptStatus.wasSuccessful(); tries++) {
                if (tries > 0) {
                    handleBeforeNextTry(millisBetweenTries, tries);
                    logger.trace("Retry4j retrying for time number {}", tries);
                }

                logger.trace("Retry4j executing callable {}", callable);
                attemptStatus = tryCall(callable);

                if (!attemptStatus.wasSuccessful()) {
                    handleFailedTry(tries + 1);
                }
            }

            refreshRetryStatus(attemptStatus.wasSuccessful(), tries);
            status.setEndTime(System.currentTimeMillis());

            postExecutionCleanup(callable, maxTries, attemptStatus);

            logger.debug("Finished retry4j execution in {} ms", status.getTotalElapsedDuration().toMillis());
            logger.trace("Finished retry4j execution with executor state {}", this);
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
                logger.trace("Throwing retries exhausted exception");
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
                logger.trace("Throwing expected exception {}", e);
                throw new UnexpectedException("Unexpected exception thrown during retry execution!", e);
            } else {
                lastKnownExceptionThatCausedRetry = e;
                attemptStatus.setSuccessful(false);
            }
        }

        return attemptStatus;
    }

    private void handleBeforeNextTry(final long millisBetweenTries, final int tries) {
        sleep(millisBetweenTries, tries);
        if (null != beforeNextTryListener) {
            beforeNextTryListener.onEvent(status);
        }
    }

    private void handleFailedTry(int tries) {
        refreshRetryStatus(false, tries);

        if (null != afterFailedTryListener) {
            afterFailedTryListener.onEvent(status);
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

        logger.trace("Retry4j executor sleeping for {} ms", millisToSleep);
        try {
            TimeUnit.MILLISECONDS.sleep(millisToSleep);
        } catch (InterruptedException ignored) {}
    }

    private boolean shouldThrowException(Exception e) {
        if (this.config.getCustomRetryOnLogic() != null) {
            //custom retry logic
            return !this.config.getCustomRetryOnLogic().apply(e);
        } else {
            //config says to always retry
            if (this.config.isRetryOnAnyException()) {
                return false;
            }

            //config says to retry only on specific exceptions
            for (Class<? extends Exception> exceptionToRetryOn : this.config.getRetryOnSpecificExceptions()) {
                if (exceptionToRetryOn.isAssignableFrom(e.getClass())) {
                    return false;
                }
            }

            //config says to retry on all except specific exceptions
            if (!this.config.getRetryOnAnyExceptionExcluding().isEmpty()) {
                for (Class<? extends Exception> exceptionToNotRetryOn : this.config.getRetryOnAnyExceptionExcluding()) {
                    if (exceptionToNotRetryOn.isAssignableFrom(e.getClass())) {
                        return true;
                    }
                }
                return false;
            }

            return true;
        }
    }

    @Override
    public void setConfig(RetryConfig config) {
        logger.trace("Set config on retry4j executor {}", config);
        this.config = config;
    }

    public CallExecutor<T> afterFailedTry(RetryListener<T> listener) {
        this.afterFailedTryListener = listener;
        return this;
    }

    public CallExecutor<T> beforeNextTry(RetryListener<T> listener) {
        this.beforeNextTryListener = listener;
        return this;
    }

    public CallExecutor<T> onCompletion(RetryListener<T> listener) {
        this.onCompletionListener = listener;
        return this;
    }

    public CallExecutor<T> onSuccess(RetryListener<T> listener) {
        this.onSuccessListener = listener;
        return this;
    }

    public CallExecutor<T> onFailure(RetryListener<T> listener) {
        this.onFailureListener = listener;
        return this;
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
