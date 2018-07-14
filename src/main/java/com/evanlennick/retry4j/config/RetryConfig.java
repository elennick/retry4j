package com.evanlennick.retry4j.config;

import com.evanlennick.retry4j.backoff.BackoffStrategy;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class RetryConfig {

    private Boolean retryOnAnyException = false;
    private Set<Class<? extends Exception>> retryOnSpecificExceptions = new HashSet<>();
    private Set<Class<? extends Exception>> retryOnAnyExceptionExcluding = new HashSet<>();
    private Integer maxNumberOfTries;
    private Duration delayBetweenRetries;
    private BackoffStrategy backoffStrategy;
    private Object valueToRetryOn;
    private Boolean retryOnValue = false;
    private Function<Exception, Boolean> customRetryOnLogic;

    public Object getValueToRetryOn() {
        return valueToRetryOn;
    }

    public void setValueToRetryOn(Object valueToRetryOn) {
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

    public Function<Exception, Boolean> getCustomRetryOnLogic() {
        return customRetryOnLogic;
    }

    public void setCustomRetryOnLogic(Function<Exception, Boolean> customRetryOnLogic) {
        this.customRetryOnLogic = customRetryOnLogic;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RetryConfig{");
        sb.append("retryOnAnyException=").append(retryOnAnyException);
        sb.append(", retryOnSpecificExceptions=").append(retryOnSpecificExceptions);
        sb.append(", retryOnAnyExceptionExcluding=").append(retryOnAnyExceptionExcluding);
        sb.append(", maxNumberOfTries=").append(maxNumberOfTries);
        sb.append(", delayBetweenRetries=").append(delayBetweenRetries);
        sb.append(", backoffStrategy=").append(backoffStrategy);
        sb.append(", valueToRetryOn=").append(valueToRetryOn);
        sb.append(", retryOnValue=").append(retryOnValue);
        sb.append(", customRetryOnLogic=").append(customRetryOnLogic);
        sb.append('}');
        return sb.toString();
    }
}
