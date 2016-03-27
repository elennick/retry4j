package com.evanlennick.retry4j;

import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.evanlennick.retry4j.listener.AfterFailedTryListener;
import com.evanlennick.retry4j.listener.BeforeNextTryListener;
import com.evanlennick.retry4j.listener.OnFailureListener;
import com.evanlennick.retry4j.listener.OnSuccessListener;
import com.evanlennick.retry4j.listener.RetryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CallExecutor {

    private Logger logger = LoggerFactory.getLogger(CallExecutor.class);

    private RetryConfig config;

    private AfterFailedTryListener afterFailedTryListener;

    private BeforeNextTryListener beforeNextTryListener;

    private OnFailureListener onFailureListener;

    private OnSuccessListener onSuccessListener;

    private ExecutorService executorService;

    private Exception lastKnownExceptionThatCausedRetry;

    private CallResults<Object> results = new CallResults<>();

    public CallExecutor() {
        this(new RetryConfigBuilder().fixedBackoff5Tries10Sec().build());
    }

    public CallExecutor(RetryConfig config) {
        this.config = config;
    }

    public CallResults<Object> execute(Callable<?> callable) throws RetriesExhaustedException, UnexpectedException {
        logger.trace("Starting retry4j execution with callable {}", config, callable);
        logger.debug("Starting retry4j execution with executor state {}", this);

        long start = System.currentTimeMillis();
        results.setStartTime(start);

        int maxTries = config.getMaxNumberOfTries();
        long millisBetweenTries = config.getDelayBetweenRetries().toMillis();
        this.results.setCallName(callable.toString());

        Optional<Object> result = Optional.empty();
        int tries;

        for (tries = 0; tries < maxTries && !result.isPresent(); tries++) {
            logger.trace("Retry4j executing callable {}", callable);
            result = tryCall(callable);

            if (!result.isPresent()) {
                handleRetry(millisBetweenTries, tries + 1);
            }

            logger.trace("Retry4j retrying for time number {}", tries);
        }

        refreshRetryResults(result.isPresent(), tries);
        results.setEndTime(System.currentTimeMillis());

        postExecutionCleanup(callable, maxTries, result);

        logger.debug("Finished retry4j execution in {} ms", results.getTotalElapsedDuration().toMillis());
        logger.trace("Finished retry4j execution with executor state {}", this);

        return results;
    }

    private void postExecutionCleanup(Callable<?> callable, int maxTries, Optional<Object> result) {
        if (!result.isPresent()) {
            String failureMsg = String.format("Call '%s' failed after %d tries!", callable.toString(), maxTries);
            if (null != onFailureListener) {
                onFailureListener.onFailure(results);
            } else {
                throw new RetriesExhaustedException(failureMsg, results);
            }
        } else {
            results.setResult(result.get());
            if (null != onSuccessListener) {
                onSuccessListener.onSuccess(results);
            }
        }
    }

    public void executeAsync(Callable<?> callable) {
        if (null == executorService) {
            executorService = Executors.newFixedThreadPool(10);
        }
        Runnable runnable = () -> this.execute(callable);
        executorService.execute(runnable);
    }

    private Optional<Object> tryCall(Callable<?> callable) throws UnexpectedException {
        try {
            Object result = callable.call();
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

        logger.trace("Retry4j executor sleeping for {} ms", millisToSleep);
        try {
            TimeUnit.MILLISECONDS.sleep(millisToSleep);
        } catch (InterruptedException ignored) {}
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

        logger.trace("Registered listener on retry4j executor {}", listener);
    }

    public void setConfig(RetryConfig config) {
        logger.trace("Set config on retry4j executor {}", config);
        this.config = config;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallExecutor{");
        sb.append("config=").append(config);
        sb.append(", afterFailedTryListener=").append(afterFailedTryListener);
        sb.append(", beforeNextTryListener=").append(beforeNextTryListener);
        sb.append(", onFailureListener=").append(onFailureListener);
        sb.append(", onSuccessListener=").append(onSuccessListener);
        sb.append(", executorService=").append(executorService);
        sb.append(", lastKnownExceptionThatCausedRetry=").append(lastKnownExceptionThatCausedRetry);
        sb.append(", results=").append(results);
        sb.append('}');
        return sb.toString();
    }
}
