package com.evanlennick.retry4j.config;

import com.evanlennick.retry4j.exception.InvalidRetryConfigException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.ConnectException;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.AssertJUnit.fail;

public class RetryConfigBuilderTest_WithValidationTest {

    private RetryConfigBuilder retryConfigBuilder;

    @BeforeMethod
    public void setup() {
        boolean isValidationEnabled = true;
        retryConfigBuilder = new RetryConfigBuilder(isValidationEnabled);
    }

    @Test
    public void verifyNoBackoffStrategyThrowsException() {
        try {
            retryConfigBuilder
                    .withMaxNumberOfTries(1)
                    .withDelayBetweenTries(1, ChronoUnit.SECONDS)
                    .build();
            fail("Expected InvalidRetryConfigException but one wasn't thrown!");
        } catch (InvalidRetryConfigException e) {
            assertThat(e.getMessage())
                    .isEqualTo(RetryConfigBuilder.MUST_SPECIFY_BACKOFF__ERROR_MSG);
        }
    }

    @Test
    public void verifyTwoBackoffStrategiesThrowsException() {
        try {
            retryConfigBuilder
                    .withMaxNumberOfTries(1)
                    .withDelayBetweenTries(1, ChronoUnit.SECONDS)
                    .withExponentialBackoff()
                    .withFibonacciBackoff()
                    .build();
            fail("Expected InvalidRetryConfigException but one wasn't thrown!");
        } catch (InvalidRetryConfigException e) {
            assertThat(e.getMessage())
                    .isEqualTo(RetryConfigBuilder.CAN_ONLY_SPECIFY_ONE_BACKOFF_STRAT__ERROR_MSG);
        }
    }

    @Test
    public void verifyNoDelayThrowsException_exponentialBackoff() {
        try {
            retryConfigBuilder
                    .withMaxNumberOfTries(1)
                    .withExponentialBackoff()
                    .build();
            fail("Expected InvalidRetryConfigException but one wasn't thrown!");
        } catch (InvalidRetryConfigException e) {
            assertThat(e.getMessage())
                    .isEqualTo("Retry config must specify the delay between retries!");
        }
    }

    @Test
    public void verifyNoDelayThrowsException_fixedBackoff() {
        try {
            retryConfigBuilder
                    .withMaxNumberOfTries(1)
                    .withFixedBackoff()
                    .build();
            fail("Expected InvalidRetryConfigException but one wasn't thrown!");
        } catch (InvalidRetryConfigException e) {
            assertThat(e.getMessage())
                    .isEqualTo("Retry config must specify the delay between retries!");
        }
    }

    @Test
    public void verifyNoDelayThrowsException_randomBackoff() {
        try {
            retryConfigBuilder
                    .withMaxNumberOfTries(1)
                    .withRandomBackoff()
                    .build();
            fail("Expected InvalidRetryConfigException but one wasn't thrown!");
        } catch (InvalidRetryConfigException e) {
            assertThat(e.getMessage())
                    .isEqualTo("Retry config must specify the delay between retries!");
        }
    }

    @Test
    public void verifyNoDelayThrowsException_fibonacciBackoff() {
        try {
            retryConfigBuilder
                    .withMaxNumberOfTries(1)
                    .withRandomBackoff()
                    .build();
            fail("Expected InvalidRetryConfigException but one wasn't thrown!");
        } catch (InvalidRetryConfigException e) {
            assertThat(e.getMessage())
                    .isEqualTo("Retry config must specify the delay between retries!");
        }
    }

    @Test
    public void verifyNoDelayDoesNotThrowException_noWaitBackoff() {
        retryConfigBuilder
                .withMaxNumberOfTries(1)
                .withNoWaitBackoff()
                .build();
    }

    @Test
    public void verifyNoMaxTriesThrowsException() {
        try {
            retryConfigBuilder
                    .withDelayBetweenTries(1, ChronoUnit.SECONDS)
                    .withExponentialBackoff()
                    .build();
            fail("Expected InvalidRetryConfigException but one wasn't thrown!");
        } catch (InvalidRetryConfigException e) {
            assertThat(e.getMessage())
                    .isEqualTo(RetryConfigBuilder.MUST_SPECIFY_MAX_TRIES__ERROR_MSG);
        }
    }

    @Test
    public void verifyTwoExceptionStrategiesThrowsException_anyAndSpecific() {
        try {
            retryConfigBuilder
                    .withMaxNumberOfTries(1)
                    .withDelayBetweenTries(1, ChronoUnit.SECONDS)
                    .withExponentialBackoff()
                    .failOnAnyException()
                    .retryOnSpecificExceptions(ConnectException.class)
                    .build();
            fail("Expected InvalidRetryConfigException but one wasn't thrown!");
        } catch (InvalidRetryConfigException e) {
            assertThat(e.getMessage())
                    .isEqualTo(RetryConfigBuilder.CAN_ONLY_SPECIFY_ONE_EXCEPTION_STRAT__ERROR_MSG);
        }
    }

    @Test
    public void verifyTwoExceptionStrategiesThrowsException_anyAndExcluding() {
        try {
            retryConfigBuilder
                    .withMaxNumberOfTries(1)
                    .withDelayBetweenTries(1, ChronoUnit.SECONDS)
                    .withExponentialBackoff()
                    .failOnAnyException()
                    .retryOnAnyExceptionExcluding(ConnectException.class)
                    .build();
            fail("Expected InvalidRetryConfigException but one wasn't thrown!");
        } catch (InvalidRetryConfigException e) {
            assertThat(e.getMessage())
                    .isEqualTo(RetryConfigBuilder.CAN_ONLY_SPECIFY_ONE_EXCEPTION_STRAT__ERROR_MSG);
        }
    }

    @Test
    public void verifyMaxRetriesSpecifiedTwiceThrowsException_numberAndIndefinite() {
        try {
            retryConfigBuilder
                    .withMaxNumberOfTries(5)
                    .retryIndefinitely()
                    .withNoWaitBackoff()
                    .failOnAnyException()
                    .build();
            fail("Expected InvalidRetryConfigException but one wasn't thrown!");
        } catch (InvalidRetryConfigException e) {
            assertThat(e.getMessage())
                    .isEqualTo(RetryConfigBuilder.ALREADY_SPECIFIED_NUMBER_OF_TRIES__ERROR_MSG);
        }
    }

    @Test
    public void verifyMaxRetriesSpecifiedTwiceThrowsException_twoNumbers() {
        try {
            retryConfigBuilder
                    .withMaxNumberOfTries(5)
                    .withMaxNumberOfTries(50)
                    .withNoWaitBackoff()
                    .failOnAnyException()
                    .build();
            fail("Expected InvalidRetryConfigException but one wasn't thrown!");
        } catch (InvalidRetryConfigException e) {
            assertThat(e.getMessage())
                    .isEqualTo(RetryConfigBuilder.ALREADY_SPECIFIED_NUMBER_OF_TRIES__ERROR_MSG);
        }
    }
}
