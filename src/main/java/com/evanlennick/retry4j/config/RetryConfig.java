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

public class RetryConfig<T> {

    private Boolean retryOnAnyException = false;
    private Set<Class<? extends Exception>> retryOnSpecificExceptions = new HashSet<>();
    private Set<Class<? extends Exception>> retryOnAnyExceptionExcluding = new HashSet<>();
    private Integer maxNumberOfTries;
    private Duration delayBetweenRetries;
    private BackoffStrategy backoffStrategy;
    private T valueToRetryOn;
    private Boolean retryOnValue = false;

    public T getValueToRetryOn() {
        return valueToRetryOn;
    }

    public void setValueToRetryOn(T valueToRetryOn) {
        this.valueToRetryOn = valueToRetryOn;
    }

    public Boolean shouldRetryOnValue() {
        return retryOnValue;
    }

    public void setRetryOnValue(Boolean retryOnValue) {
        this.retryOnValue = retryOnValue;
    }

    public Boolean isRetryOnAnyException() {
        return retryOnAnyException;
    }

    public void setRetryOnAnyException(boolean retryOnAnyException) {
        this.retryOnAnyException = retryOnAnyException;
    }

    public Set<Class<? extends Exception>> getRetryOnSpecificExceptions() {
        return retryOnSpecificExceptions;
    }

    public void setRetryOnSpecificExceptions(Set<Class<? extends Exception>> retryOnSpecificExceptions) {
        this.retryOnSpecificExceptions = retryOnSpecificExceptions;
    }

    public Set<Class<? extends Exception>> getRetryOnAnyExceptionExcluding() {
        return retryOnAnyExceptionExcluding;
    }

    public void setRetryOnAnyExceptionExcluding(Set<Class<? extends Exception>> retryOnAnyExceptionExcluding) {
        this.retryOnAnyExceptionExcluding = retryOnAnyExceptionExcluding;
    }

    public Integer getMaxNumberOfTries() {
        return maxNumberOfTries;
    }

    public void setMaxNumberOfTries(int maxNumberOfTries) {
        if (maxNumberOfTries < 0) {
            throw new IllegalArgumentException("Must be a non-negative number.");
        }

        this.maxNumberOfTries = maxNumberOfTries;
    }

    public Duration getDelayBetweenRetries() {
        return delayBetweenRetries;
    }

    public void setDelayBetweenRetries(Duration delayBetweenRetries) {
        if (delayBetweenRetries.isNegative()) {
            throw new IllegalArgumentException("Must be a non-negative Duration.");
        }

        this.delayBetweenRetries = delayBetweenRetries;
    }

    public BackoffStrategy getBackoffStrategy() {
        return backoffStrategy;
    }

    public void setBackoffStrategy(BackoffStrategy backoffStrategy) {
        this.backoffStrategy = backoffStrategy;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RetryConfig{");
        sb.append("retryOnAnyException=").append(retryOnAnyException);
        sb.append(", retryOnSpecificExceptions=").append(retryOnSpecificExceptions);
        sb.append(", maxNumberOfTries=").append(maxNumberOfTries);
        sb.append(", delayBetweenRetries=").append(delayBetweenRetries);
        sb.append(", backoffStrategy=").append(backoffStrategy);
        sb.append('}');
        return sb.toString();
    }

    public static <T> RetryConfig.RetryConfigBuilder<T> builder() {
        return new RetryConfig.RetryConfigBuilder();
    }

    public static class RetryConfigBuilder<T> {

        private boolean exceptionStrategySpecified;
        private RetryConfig config;
        private boolean validationEnabled;

        public static final String MUST_SPECIFY_BACKOFF__ERROR_MSG
                = "Retry config must specify a backoff strategy!";
        public static final String MUST_SPECIFY_MAX_TRIES__ERROR_MSG
                = "Retry config must specify a maximum number of tries!";
        public static final String CAN_ONLY_SPECIFY_ONE_BACKOFF_STRAT__ERROR_MSG
                = "Retry config cannot specify more than one backoff strategy!";
        public static final String CAN_ONLY_SPECIFY_ONE_EXCEPTION_STRAT__ERROR_MSG
                = "Retry config cannot specify more than one exception strategy!";
        public static final String ALREADY_SPECIFIED_NUMBER_OF_TRIES__ERROR_MSG
                = "Number of tries can only be specified once!";

        public RetryConfigBuilder() {
            this.config = new RetryConfig();
            this.exceptionStrategySpecified = false;
            this.validationEnabled = true;
        }

        public RetryConfigBuilder(boolean validationEnabled) {
            this();
            this.validationEnabled = validationEnabled;
        }

        public RetryConfig.RetryConfigBuilder withValidationDisabled() {
            this.validationEnabled = false;
            return this;
        }

        public RetryConfig.RetryConfigBuilder retryOnAnyException() {
            validateExceptionStrategyAddition();

            config.setRetryOnAnyException(true);

            exceptionStrategySpecified = true;
            return this;
        }

        public RetryConfig.RetryConfigBuilder failOnAnyException() {
            validateExceptionStrategyAddition();

            config.setRetryOnAnyException(false);
            config.setRetryOnSpecificExceptions(new HashSet<>());

            exceptionStrategySpecified = true;
            return this;
        }

        @SafeVarargs
        public final RetryConfig.RetryConfigBuilder retryOnSpecificExceptions(Class<? extends Exception>... exceptions) {
            validateExceptionStrategyAddition();

            Set<Class<? extends Exception>> setOfExceptions = new HashSet<>(Arrays.asList(exceptions));
            config.setRetryOnSpecificExceptions(setOfExceptions);

            exceptionStrategySpecified = true;
            return this;
        }

        @SafeVarargs
        public final RetryConfig.RetryConfigBuilder retryOnAnyExceptionExcluding(Class<? extends Exception>... exceptions) {
            validateExceptionStrategyAddition();

            Set<Class<? extends Exception>> setOfExceptions = new HashSet<>(Arrays.asList(exceptions));
            config.setRetryOnAnyExceptionExcluding(setOfExceptions);

            exceptionStrategySpecified = true;
            return this;
        }

        public final RetryConfig.RetryConfigBuilder retryOnReturnValue(T value) {
            config.setRetryOnValue(true);
            config.setValueToRetryOn(value);

            return this;
        }

        public RetryConfig.RetryConfigBuilder withMaxNumberOfTries(int max) {
            if (config.getMaxNumberOfTries() != null) {
                throw new InvalidRetryConfigException(ALREADY_SPECIFIED_NUMBER_OF_TRIES__ERROR_MSG);
            }

            config.setMaxNumberOfTries(max);
            return this;
        }

        public RetryConfig.RetryConfigBuilder retryIndefinitely() {
            if (config.getMaxNumberOfTries() != null) {
                throw new InvalidRetryConfigException(ALREADY_SPECIFIED_NUMBER_OF_TRIES__ERROR_MSG);
            }

            config.setMaxNumberOfTries(Integer.MAX_VALUE);
            return this;
        }

        public RetryConfig.RetryConfigBuilder withDelayBetweenTries(Duration duration) {
            config.setDelayBetweenRetries(duration);
            return this;
        }

        public RetryConfig.RetryConfigBuilder withDelayBetweenTries(long amount, ChronoUnit time) {
            config.setDelayBetweenRetries(Duration.of(amount, time));
            return this;
        }

        public RetryConfig.RetryConfigBuilder withBackoffStrategy(BackoffStrategy backoffStrategy) {
            validateBackoffStrategyAddition();
            config.setBackoffStrategy(backoffStrategy);
            return this;
        }

        public RetryConfig.RetryConfigBuilder withFixedBackoff() {
            validateBackoffStrategyAddition();
            config.setBackoffStrategy(new FixedBackoffStrategy());
            return this;
        }

        public RetryConfig.RetryConfigBuilder withExponentialBackoff() {
            validateBackoffStrategyAddition();
            config.setBackoffStrategy(new ExponentialBackoffStrategy());
            return this;
        }

        public RetryConfig.RetryConfigBuilder withFibonacciBackoff() {
            validateBackoffStrategyAddition();
            config.setBackoffStrategy(new FibonacciBackoffStrategy());
            return this;
        }

        public RetryConfig.RetryConfigBuilder withNoWaitBackoff() {
            validateBackoffStrategyAddition();
            config.setBackoffStrategy(new NoWaitBackoffStrategy());
            return this;
        }

        public RetryConfig.RetryConfigBuilder withRandomBackoff() {
            validateBackoffStrategyAddition();
            config.setBackoffStrategy(new RandomBackoffStrategy());
            return this;
        }

        public RetryConfig.RetryConfigBuilder withRandomExponentialBackoff() {
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
}
