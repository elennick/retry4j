package com.evanlennick.retry4j.config;

import com.evanlennick.retry4j.backoff.*;
import com.evanlennick.retry4j.exception.InvalidRetryConfigException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class AlternateRetryConfigBuilder {

  private Object valueToRetryOn;
  private Boolean retryOnValue = false;
  // Using some defaults here, but we need not declare defaults and validate for nulls during `build()`
  private BackoffStrategy backoffStrategy = BackoffStrategyRegistry.fixedBackoff;
  private ExceptionRetryConfig exceptionRetryConfig = new ExceptionRetryConfig()
      .retryOnAnyException();
  private TimingRetryConfig timingRetryConfig = TimingRetryConfig.retryIndefinitely()
      .withDelayBetweenTries(2, ChronoUnit.SECONDS);

  private AlternateRetryConfigBuilder() {
  }

  public static AlternateRetryConfigBuilder exceptionRetryConfig(ExceptionRetryConfig exceptionRetryConfig) {
    AlternateRetryConfigBuilder builder = new AlternateRetryConfigBuilder();
    builder.exceptionRetryConfig = exceptionRetryConfig;
    return builder;
  }

  public static AlternateRetryConfigBuilder retryOnReturnValue(Object value) {
    AlternateRetryConfigBuilder builder = new AlternateRetryConfigBuilder();
    builder.valueToRetryOn = value;
    builder.retryOnValue = true;
    return builder;
  }

  public static class ExceptionRetryConfig {
    private Boolean retryOnAnyException = false;
    private Function<Exception, Boolean> customRetryOnLogic;
    private ExceptionsCriteria exceptionsCriteriaBuilder = new ExceptionsCriteria();

    public static ExceptionRetryConfig retryOnAnyException() {
      ExceptionRetryConfig config = new ExceptionRetryConfig();
      config.retryOnAnyException = true;
      return config;
    }

    public static ExceptionRetryConfig failOnAnyException() {
      ExceptionRetryConfig config = new ExceptionRetryConfig();
      config.retryOnAnyException = false;
      return config;
    }

    public static ExceptionRetryConfig retryOnExceptions(ExceptionsCriteria exceptionsCriteriaBuilder) {
      ExceptionRetryConfig config = new ExceptionRetryConfig();
      config.exceptionsCriteriaBuilder = exceptionsCriteriaBuilder;
      return config;
    }

    public static ExceptionRetryConfig retryOnCustomExceptionLogic(Function<Exception, Boolean> customRetryFunction) {
      ExceptionRetryConfig config = new ExceptionRetryConfig();
      config.customRetryOnLogic = customRetryFunction;
      return config;
    }
  }

  public static class ExceptionsCriteria {
    private Set<Class<? extends Exception>> retryOnSpecificExceptions = new HashSet<>();
    private Set<Class<? extends Exception>> retryOnAnyExceptionExcluding = new HashSet<>();
    private boolean retryOnCausedBy;

    public ExceptionsCriteria retryOnSpecificExceptions(Class<? extends Exception>... exceptions) {
      this.retryOnSpecificExceptions = new HashSet<>(Arrays.asList(exceptions));
      return this;
    }

    public ExceptionsCriteria retryOnAnyExceptionExcluding(Class<? extends Exception>... exceptions) {
      this.retryOnAnyExceptionExcluding = new HashSet<>(Arrays.asList(exceptions));
      return this;
    }

    public ExceptionsCriteria retryOnCausedBy() {
      this.retryOnCausedBy = true;
      return this;
    }
  }

  public static class TimingRetryConfig {

    private Integer maxNumberOfTries;
    private Duration delayBetweenRetries;
    public static final String SHOULD_SPECIFY_DELAY_BETWEEN_RETRIES_AS_POSTIVE__ERROR_MSG
        = "Delay between retries must be a non-negative Duration.";

    private TimingRetryConfig() {
    }

    public static TimingRetryConfig withMaxTries(int maxTries) {
      TimingRetryConfig config = new TimingRetryConfig();
      config.maxNumberOfTries = maxTries;
      return config;
    }

    public static TimingRetryConfig retryIndefinitely() {
      TimingRetryConfig config = new TimingRetryConfig();
      config.maxNumberOfTries = Integer.MAX_VALUE;
      return config;
    }


    public TimingRetryConfig withDelayBetweenTries(Duration duration) {
      if (duration.isNegative()) {
        throw new InvalidRetryConfigException(SHOULD_SPECIFY_DELAY_BETWEEN_RETRIES_AS_POSTIVE__ERROR_MSG);
      }

      delayBetweenRetries = duration;
      return this;
    }

    public TimingRetryConfig withDelayBetweenTries(long amount, ChronoUnit time) {
      delayBetweenRetries = Duration.of(amount, time);
      return this;
    }

  }

  public static class BackoffStrategyRegistry {
    public static final BackoffStrategy fixedBackoff =  new FixedBackoffStrategy();
    public static final BackoffStrategy exponentialBackoff = new ExponentialBackoffStrategy();
    public static final BackoffStrategy fibonacciBackoff =  new FibonacciBackoffStrategy();
    public static final BackoffStrategy noWaitBackoff= new NoWaitBackoffStrategy();
    public static final BackoffStrategy randomBackoff=  new RandomBackoffStrategy();
    public static final BackoffStrategy randomExponentialBackoff =  new RandomExponentialBackoffStrategy();
  }

  public AlternateRetryConfigBuilder withBackoffStrategy(BackoffStrategy backoffStrategy) {
    this.backoffStrategy = backoffStrategy;
    return this;
  }

  public AlternateRetryConfigBuilder withExceptionRetryconfig(ExceptionRetryConfig exceptionRetryConfig) {
    this.exceptionRetryConfig = exceptionRetryConfig;
    return this;
  }

  public AlternateRetryConfigBuilder withTimingRetryConfig(TimingRetryConfig timingRetryConfig) {
    this.timingRetryConfig = timingRetryConfig;
    return this;
  }

  public RetryConfig build() {
    RetryConfig retryConfig = new RetryConfig(
        exceptionRetryConfig.retryOnAnyException, exceptionRetryConfig.exceptionsCriteriaBuilder.retryOnSpecificExceptions,
        exceptionRetryConfig.exceptionsCriteriaBuilder.retryOnAnyExceptionExcluding, timingRetryConfig.maxNumberOfTries,
        timingRetryConfig.delayBetweenRetries, backoffStrategy, valueToRetryOn, retryOnValue,
        exceptionRetryConfig.customRetryOnLogic, exceptionRetryConfig.exceptionsCriteriaBuilder.retryOnCausedBy
    );
    return retryConfig;
  }

}

