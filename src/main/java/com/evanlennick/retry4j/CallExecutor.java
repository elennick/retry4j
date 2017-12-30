package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.evanlennick.retry4j.listener.AfterFailedTryListener;
import com.evanlennick.retry4j.listener.BeforeNextTryListener;
import com.evanlennick.retry4j.listener.OnCompletionListener;
import com.evanlennick.retry4j.listener.OnFailureListener;
import com.evanlennick.retry4j.listener.OnSuccessListener;
import com.evanlennick.retry4j.listener.RetryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CallExecutor<T> {

    private Logger logger = LoggerFactory.getLogger(CallExecutor.class);

    private RetryConfig config;

    private AfterFailedTryListener afterFailedTryListener;

    private BeforeNextTryListener beforeNextTryListener;

    private OnFailureListener onFailureListener;

    private OnSuccessListener onSuccessListener;

    private OnCompletionListener onCompletionListener;

    private ExecutorService executorService;

    private Exception lastKnownExceptionThatCausedRetry;

    private CallResults<T> results = new CallResults<>();

    public CallExecutor() {
        this(new RetryConfigBuilder().fixedBackoff5Tries10Sec().build());
    }

    public CallExecutor(RetryConfig config) {
        this.config = config;
    }

    public CallResults<T> execute(Callable<T> callable) throws RetriesExhaustedException, UnexpectedException {
        logger.trace("Starting retry4j execution with callable {}", config, callable);
        logger.debug("Starting retry4j execution with executor state {}", this);

        long start = System.currentTimeMillis();
        results.setStartTime(start);

        int maxTries = config.getMaxNumberOfTries();
        long millisBetweenTries = config.getDelayBetweenRetries().toMillis();
        this.results.setCallName(callable.toString());

        AttemptResults<T> attemptResults = new AttemptResults<>();
        attemptResults.setSuccessful(false);

        int tries;

        try {
            for (tries = 0; tries < maxTries && !attemptResults.wasSuccessful(); tries++) {
                logger.trace("Retry4j executing callable {}", callable);
                attemptResults = tryCall(callable);

                if (!attemptResults.wasSuccessful()) {
                    handleRetry(millisBetweenTries, tries + 1);
                }

                logger.trace("Retry4j retrying for time number {}", tries);
            }

            refreshRetryResults(attemptResults.wasSuccessful(), tries);
            results.setEndTime(System.currentTimeMillis());

            postExecutionCleanup(callable, maxTries, attemptResults);

            logger.debug("Finished retry4j execution in {} ms", results.getTotalElapsedDuration().toMillis());
            logger.trace("Finished retry4j execution with executor state {}", this);
        } finally {
            if (null != onCompletionListener) {
                onCompletionListener.onCompletion(results);
            }
        }

        return results;
    }

    public void executeAsync(Callable<T> callable) {
        if (null == executorService) {
            executorService = Executors.newFixedThreadPool(10);
        }
        Runnable runnable = () -> this.execute(callable);
        executorService.execute(runnable);
    }

    private void postExecutionCleanup(Callable<T> callable, int maxTries, AttemptResults<T> result) {
        if (!result.wasSuccessful()) {
            String failureMsg = String.format("Call '%s' failed after %d tries!", callable.toString(), maxTries);
            if (null != onFailureListener) {
                onFailureListener.onFailure(results);
            } else {
                logger.trace("Throwing retries exhausted exception");
                throw new RetriesExhaustedException(failureMsg, lastKnownExceptionThatCausedRetry, results);
            }
        } else {
            results.setResult(result.getResult());
            if (null != onSuccessListener) {
                onSuccessListener.onSuccess(results);
            }
        }
    }

    private AttemptResults<T> tryCall(Callable<T> callable) throws UnexpectedException {
        AttemptResults attemptResult = new AttemptResults();

        try {
            T callResult = callable.call();

            boolean shouldRetryOnThisResult
                    = config.shouldRetryOnValue() && callResult.equals(config.getValueToRetryOn());
            if (shouldRetryOnThisResult) {
                attemptResult.setSuccessful(false);
            } else {
                attemptResult.setResult(callResult);
                attemptResult.setSuccessful(true);
            }
        } catch (Exception e) {
            if (shouldThrowException(e)) {
                logger.trace("Throwing expected exception {}", e);
                throw new UnexpectedException(e);
            } else {
                lastKnownExceptionThatCausedRetry = e;
                attemptResult.setSuccessful(false);
            }
        }

        return attemptResult;
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

    public void registerRetryListener(RetryListener listener) {
        if (listener instanceof AfterFailedTryListener) {
            this.afterFailedTryListener = (AfterFailedTryListener) listener;
        } else if (listener instanceof BeforeNextTryListener) {
            this.beforeNextTryListener = (BeforeNextTryListener) listener;
        } else if (listener instanceof OnSuccessListener) {
            this.onSuccessListener = (OnSuccessListener) listener;
        } else if (listener instanceof OnFailureListener) {
            this.onFailureListener = (OnFailureListener) listener;
        } else if (listener instanceof OnCompletionListener) {
            this.onCompletionListener = (OnCompletionListener) listener;
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
