package com.evanlennick.retry4j.config;

import com.evanlennick.retry4j.backoff.BackoffStrategy;
import com.evanlennick.retry4j.backoff.ExponentialBackoffStrategy;
import com.evanlennick.retry4j.backoff.FibonacciBackoffStrategy;
import com.evanlennick.retry4j.backoff.FixedBackoffStrategy;
import com.evanlennick.retry4j.backoff.NoWaitBackoffStrategy;
import com.evanlennick.retry4j.backoff.RandomBackoffStrategy;
import com.evanlennick.retry4j.backoff.RandomExponentialBackoffStrategy;
import com.evanlennick.retry4j.exception.InvalidRetryConfigException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.time.temporal.ChronoUnit.SECONDS;

public class RetryConfigBuilder<T> {

    private boolean exceptionStrategySpecified;
    private RetryConfig<T> config;
    private boolean validationEnabled;

    public final static String MUST_SPECIFY_BACKOFF__ERROR_MSG
            = "Retry config must specify a backoff strategy!";
    public final static String MUST_SPECIFY_MAX_TRIES__ERROR_MSG
            = "Retry config must specify a maximum number of tries!";
    public final static String CAN_ONLY_SPECIFY_ONE_BACKOFF_STRAT__ERROR_MSG
            = "Retry config cannot specify more than one backoff strategy!";
    public final static String CAN_ONLY_SPECIFY_ONE_EXCEPTION_STRAT__ERROR_MSG
            = "Retry config cannot specify more than one exception strategy!";
    public final static String ALREADY_SPECIFIED_NUMBER_OF_TRIES__ERROR_MSG
            = "Number of tries can only be specified once!";

    public RetryConfigBuilder() {
        this.config = new RetryConfig<>();
        this.exceptionStrategySpecified = false;
        this.validationEnabled = true;
    }

    public RetryConfigBuilder(boolean validationEnabled) {
        this();
        this.validationEnabled = validationEnabled;
    }

    public boolean isValidationEnabled() {
        return validationEnabled;
    }

    public void setValidationEnabled(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
    }

    public RetryConfigBuilder<T> retryOnAnyException() {
        validateExceptionStrategyAddition();

        config.setRetryOnAnyException(true);

        exceptionStrategySpecified = true;
        return this;
    }

    public RetryConfigBuilder<T> failOnAnyException() {
        validateExceptionStrategyAddition();

        config.setRetryOnAnyException(false);
        config.setRetryOnSpecificExceptions(new HashSet<>());

        exceptionStrategySpecified = true;
        return this;
    }

    @SafeVarargs
    public final RetryConfigBuilder<T> retryOnSpecificExceptions(Class<? extends Exception>... exceptions) {
        validateExceptionStrategyAddition();

        Set<Class<? extends Exception>> setOfExceptions = new HashSet<>(Arrays.asList(exceptions));
        config.setRetryOnSpecificExceptions(setOfExceptions);

        exceptionStrategySpecified = true;
        return this;
    }

    @SafeVarargs
    public final RetryConfigBuilder<T> retryOnAnyExceptionExcluding(Class<? extends Exception>... exceptions) {
        validateExceptionStrategyAddition();

        Set<Class<? extends Exception>> setOfExceptions = new HashSet<>(Arrays.asList(exceptions));
        config.setRetryOnAnyExceptionExcluding(setOfExceptions);

        exceptionStrategySpecified = true;
        return this;
    }

    public final RetryConfigBuilder<T> retryOnReturnValue(T value) {
        config.setRetryOnValue(true);
        config.setValueToRetryOn(value);

        return this;
    }

    public RetryConfigBuilder<T> withMaxNumberOfTries(int max) {
        if (config.getMaxNumberOfTries() != null) {
            throw new InvalidRetryConfigException(ALREADY_SPECIFIED_NUMBER_OF_TRIES__ERROR_MSG);
        }

        config.setMaxNumberOfTries(max);
        return this;
    }

    public RetryConfigBuilder<T> retryIndefinitely() {
        if (config.getMaxNumberOfTries() != null) {
            throw new InvalidRetryConfigException(ALREADY_SPECIFIED_NUMBER_OF_TRIES__ERROR_MSG);
        }

        config.setMaxNumberOfTries(Integer.MAX_VALUE);
        return this;
    }

    public RetryConfigBuilder<T> withDelayBetweenTries(Duration duration) {
        config.setDelayBetweenRetries(duration);
        return this;
    }

    public RetryConfigBuilder<T> withDelayBetweenTries(long amount, ChronoUnit time) {
        config.setDelayBetweenRetries(Duration.of(amount, time));
        return this;
    }

    public RetryConfigBuilder<T> withBackoffStrategy(BackoffStrategy backoffStrategy) {
        validateBackoffStrategyAddition();
        config.setBackoffStrategy(backoffStrategy);
        return this;
    }

    public RetryConfigBuilder<T> withFixedBackoff() {
        validateBackoffStrategyAddition();
        config.setBackoffStrategy(new FixedBackoffStrategy());
        return this;
    }

    public RetryConfigBuilder<T> withExponentialBackoff() {
        validateBackoffStrategyAddition();
        config.setBackoffStrategy(new ExponentialBackoffStrategy());
        return this;
    }

    public RetryConfigBuilder<T> withFibonacciBackoff() {
        validateBackoffStrategyAddition();
        config.setBackoffStrategy(new FibonacciBackoffStrategy());
        return this;
    }

    public RetryConfigBuilder<T> withNoWaitBackoff() {
        validateBackoffStrategyAddition();
        config.setBackoffStrategy(new NoWaitBackoffStrategy());
        return this;
    }

    public RetryConfigBuilder<T> withRandomBackoff() {
        validateBackoffStrategyAddition();
        config.setBackoffStrategy(new RandomBackoffStrategy());
        return this;
    }

    public RetryConfigBuilder<T> withRandomExponentialBackoff() {
        validateBackoffStrategyAddition();
        config.setBackoffStrategy(new RandomExponentialBackoffStrategy());
        return this;
    }

    public RetryConfig<T> build() {
        validateConfig();

        return config;
    }

    private void validateConfig() {
        if (!validationEnabled) {
            return;
        }

        if (null == config.getBackoffStrategy()) {
            throw new InvalidRetryConfigException(MUST_SPECIFY_BACKOFF__ERROR_MSG);
        }

        if (null == config.getMaxNumberOfTries()) {
            throw new InvalidRetryConfigException(MUST_SPECIFY_MAX_TRIES__ERROR_MSG);
        }

        config.getBackoffStrategy().validateConfig(config);
    }

    private void validateBackoffStrategyAddition() {
        if (!validationEnabled) {
            return;
        }

        if (null != config.getBackoffStrategy()) {
            throw new InvalidRetryConfigException(CAN_ONLY_SPECIFY_ONE_BACKOFF_STRAT__ERROR_MSG);
        }
    }

    private void validateExceptionStrategyAddition() {
        if (!validationEnabled) {
            return;
        }

        if (exceptionStrategySpecified) {
            throw new InvalidRetryConfigException(CAN_ONLY_SPECIFY_ONE_EXCEPTION_STRAT__ERROR_MSG);
        }
    }

    public RetryConfigBuilder<T> fixedBackoff5Tries10Sec() {
        return new RetryConfigBuilder<T>()
                .retryOnAnyException()
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(10, SECONDS)
                .withFixedBackoff();
    }

    public RetryConfigBuilder<T> exponentialBackoff5Tries5Sec() {
        return new RetryConfigBuilder<T>()
                .retryOnAnyException()
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(5, SECONDS)
                .withExponentialBackoff();
    }

    public RetryConfigBuilder<T> fiboBackoff7Tries5Sec() {
        return new RetryConfigBuilder<T>()
                .retryOnAnyException()
                .withMaxNumberOfTries(7)
                .withDelayBetweenTries(5, SECONDS)
                .withFibonacciBackoff();
    }

    public RetryConfigBuilder<T> randomExpBackoff10Tries60Sec() {
        return new RetryConfigBuilder<T>()
                .retryOnAnyException()
                .withMaxNumberOfTries(10)
                .withDelayBetweenTries(60, SECONDS)
                .withRandomExponentialBackoff();
    }

}
