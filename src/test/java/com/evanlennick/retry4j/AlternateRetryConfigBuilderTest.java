package com.evanlennick.retry4j;


import com.evanlennick.retry4j.config.AlternateRetryConfigBuilder;
import com.evanlennick.retry4j.config.AlternateRetryConfigBuilder.BackoffStrategyRegistry;
import com.evanlennick.retry4j.config.AlternateRetryConfigBuilder.ExceptionRetryConfig;
import com.evanlennick.retry4j.config.AlternateRetryConfigBuilder.ExceptionsCriteria;
import com.evanlennick.retry4j.config.AlternateRetryConfigBuilder.TimingRetryConfig;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.temporal.ChronoUnit;

public class AlternateRetryConfigBuilderTest {

  @Test
  public void testConfig1() {
    AlternateRetryConfigBuilder
        .exceptionRetryConfig(
            new ExceptionRetryConfig().retryOnAnyException()
        )
        .withTimingRetryConfig(
            TimingRetryConfig.withMaxTries(200)
        )
        .withBackoffStrategy(BackoffStrategyRegistry.noWaitBackoff)
        .build();
  }

  @Test
  public void testConfig2() {
    AlternateRetryConfigBuilder
        .exceptionRetryConfig(
            new ExceptionRetryConfig().retryOnExceptions(new ExceptionsCriteria()
                .retryOnSpecificExceptions(IllegalArgumentException.class))
        )
        .withTimingRetryConfig(
            TimingRetryConfig.retryIndefinitely().withDelayBetweenTries(3, ChronoUnit.MINUTES)
        )
        .withBackoffStrategy(BackoffStrategyRegistry.exponentialBackoff)
        .build();
  }

  @Test
  public void testConfig3() {
    AlternateRetryConfigBuilder
        .exceptionRetryConfig(
            new ExceptionRetryConfig().retryOnExceptions(new ExceptionsCriteria()
                .retryOnSpecificExceptions(IOException.class)
                .retryOnAnyExceptionExcluding(FileNotFoundException.class)
                .retryOnCausedBy())
        )
        .withTimingRetryConfig(
            TimingRetryConfig.retryIndefinitely().withDelayBetweenTries(3, ChronoUnit.MINUTES)
        )
        .withBackoffStrategy(BackoffStrategyRegistry.exponentialBackoff)
        .build();
  }

  @Test
  public void testConfig4() {
    AlternateRetryConfigBuilder
        .retryOnReturnValue("retry on this value!")
        .withTimingRetryConfig(
            TimingRetryConfig.retryIndefinitely()
        )
        .withBackoffStrategy(BackoffStrategyRegistry.fibonacciBackoff)
        .build();
  }

}
