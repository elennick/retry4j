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
import java.util.function.Function;

import static java.time.temporal.ChronoUnit.SECONDS;

public class RetryConfigBuilder {

    private boolean builtInExceptionStrategySpecified;
    private RetryConfig config;
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
    public final static String CAN_ONLY_SPECIFY_CUSTOM_EXCEPTION_STRAT__ERROR_MSG
            = "You cannot use built in exception logic and custom exception logic in the same config!";

    public RetryConfigBuilder() {
        this.config = new RetryConfig();
        this.builtInExceptionStrategySpecified = false;
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

    public RetryConfigBuilder retryOnAnyException() {
        validateExceptionStrategyAddition();

        config.setRetryOnAnyException(true);

        builtInExceptionStrategySpecified = true;
        return this;
    }

    public RetryConfigBuilder failOnAnyException() {
        validateExceptionStrategyAddition();

        config.setRetryOnAnyException(false);
        config.setRetryOnSpecificExceptions(new HashSet<>());

        builtInExceptionStrategySpecified = true;
        return this;
    }

    @SafeVarargs
    public final RetryConfigBuilder retryOnSpecificExceptions(Class<? extends Exception>... exceptions) {
        validateExceptionStrategyAddition();

        Set<Class<? extends Exception>> setOfExceptions = new HashSet<>(Arrays.asList(exceptions));
        config.setRetryOnSpecificExceptions(setOfExceptions);

        builtInExceptionStrategySpecified = true;
        return this;
    }

    @SafeVarargs
    public final RetryConfigBuilder retryOnAnyExceptionExcluding(Class<? extends Exception>... exceptions) {
        validateExceptionStrategyAddition();

        Set<Class<? extends Exception>> setOfExceptions = new HashSet<>(Arrays.asList(exceptions));
        config.setRetryOnAnyExceptionExcluding(setOfExceptions);

        builtInExceptionStrategySpecified = true;
        return this;
    }

    public final RetryConfigBuilder retryOnReturnValue(Object value) {
        config.setRetryOnValue(true);
        config.setValueToRetryOn(value);

        return this;
    }

    public RetryConfigBuilder retryOnCustomExceptionLogic(Function<Exception, Boolean> customRetryFunction) {
        this.config.setCustomRetryOnLogic(customRetryFunction);
        return this;
    }

    public RetryConfigBuilder withMaxNumberOfTries(int max) {
        if (config.getMaxNumberOfTries() != null) {
            throw new InvalidRetryConfigException(ALREADY_SPECIFIED_NUMBER_OF_TRIES__ERROR_MSG);
        }

        config.setMaxNumberOfTries(max);
        return this;
    }

    public RetryConfigBuilder retryIndefinitely() {
        if (config.getMaxNumberOfTries() != null) {
            throw new InvalidRetryConfigException(ALREADY_SPECIFIED_NUMBER_OF_TRIES__ERROR_MSG);
        }

        config.setMaxNumberOfTries(Integer.MAX_VALUE);
        return this;
    }

    public RetryConfigBuilder withDelayBetweenTries(Duration duration) {
        config.setDelayBetweenRetries(duration);
        return this;
    }

    public RetryConfigBuilder withDelayBetweenTries(long amount, ChronoUnit time) {
        config.setDelayBetweenRetries(Duration.of(amount, time));
        return this;
    }

    public RetryConfigBuilder withBackoffStrategy(BackoffStrategy backoffStrategy) {
        validateBackoffStrategyAddition();
        config.setBackoffStrategy(backoffStrategy);
        return this;
    }

    public RetryConfigBuilder withFixedBackoff() {
        validateBackoffStrategyAddition();
        config.setBackoffStrategy(new FixedBackoffStrategy());
        return this;
    }

    public RetryConfigBuilder withExponentialBackoff() {
        validateBackoffStrategyAddition();
        config.setBackoffStrategy(new ExponentialBackoffStrategy());
        return this;
    }

    public RetryConfigBuilder withFibonacciBackoff() {
        validateBackoffStrategyAddition();
        config.setBackoffStrategy(new FibonacciBackoffStrategy());
        return this;
    }

    public RetryConfigBuilder withNoWaitBackoff() {
        validateBackoffStrategyAddition();
        config.setBackoffStrategy(new NoWaitBackoffStrategy());
        return this;
    }

    public RetryConfigBuilder withRandomBackoff() {
        validateBackoffStrategyAddition();
        config.setBackoffStrategy(new RandomBackoffStrategy());
        return this;
    }

    public RetryConfigBuilder withRandomExponentialBackoff() {
        validateBackoffStrategyAddition();
        config.setBackoffStrategy(new RandomExponentialBackoffStrategy());
        return this;
    }

    public RetryConfig build() {
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

        if (null != config.getCustomRetryOnLogic() && builtInExceptionStrategySpecified) {
            throw new InvalidRetryConfigException(CAN_ONLY_SPECIFY_CUSTOM_EXCEPTION_STRAT__ERROR_MSG);
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

        if (builtInExceptionStrategySpecified) {
            throw new InvalidRetryConfigException(CAN_ONLY_SPECIFY_ONE_EXCEPTION_STRAT__ERROR_MSG);
        }
    }

    public RetryConfigBuilder fixedBackoff5Tries10Sec() {
        return new RetryConfigBuilder()
                .retryOnAnyException()
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(10, SECONDS)
                .withFixedBackoff();
    }

    public RetryConfigBuilder exponentialBackoff5Tries5Sec() {
        return new RetryConfigBuilder()
                .retryOnAnyException()
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(5, SECONDS)
                .withExponentialBackoff();
    }

    public RetryConfigBuilder fiboBackoff7Tries5Sec() {
        return new RetryConfigBuilder()
                .retryOnAnyException()
                .withMaxNumberOfTries(7)
                .withDelayBetweenTries(5, SECONDS)
                .withFibonacciBackoff();
    }

    public RetryConfigBuilder randomExpBackoff10Tries60Sec() {
        return new RetryConfigBuilder()
                .retryOnAnyException()
                .withMaxNumberOfTries(10)
                .withDelayBetweenTries(60, SECONDS)
                .withRandomExponentialBackoff();
    }

}
