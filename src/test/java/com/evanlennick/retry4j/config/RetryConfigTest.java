package com.evanlennick.retry4j.config;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class RetryConfigTest {

    private RetryConfig config;

    @BeforeMethod
    public void setup() {
        this.config = new RetryConfig();
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void shouldNotAllowNegativeDelayBetweenRetries() {
        config.setDelayBetweenRetries(Duration.of(-1, ChronoUnit.SECONDS));
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void shouldNotAllowNegativeMaxNumberOfTries() {
        config.setMaxNumberOfTries(-1);
    }
}
