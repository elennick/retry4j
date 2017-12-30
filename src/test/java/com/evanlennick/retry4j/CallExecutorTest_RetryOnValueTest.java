package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.AssertJUnit.fail;

public class CallExecutorTest_RetryOnValueTest {

    private RetryConfigBuilder retryConfigBuilder;

    @BeforeMethod
    public void setup() {
        boolean configValidationEnabled = true;
        this.retryConfigBuilder = new RetryConfigBuilder(configValidationEnabled);
    }

    @Test
    public void verifyRetryOnStringValue_shouldRetry() {
        Callable<String> callable = () -> "should retry!";

        RetryConfig config = retryConfigBuilder
                .retryOnAnyException()
                .retryOnReturnValue("should retry!")
                .withDelayBetweenTries(Duration.ZERO)
                .withMaxNumberOfTries(3)
                .withFixedBackoff()
                .build();

        assertRetryOccurs(config, callable);
    }

    @Test
    public void verifyRetryOnStringValue_shouldNotRetry() {
        Callable<String> callable = () -> "should NOT retry!";

        RetryConfig config = retryConfigBuilder
                .retryOnAnyException()
                .retryOnReturnValue("should retry!")
                .withDelayBetweenTries(Duration.ZERO)
                .withMaxNumberOfTries(3)
                .withFixedBackoff()
                .build();

        assertRetryDoesNotOccur(config, callable);
    }

    @Test
    public void verifyRetryOnBooleanValue_shouldRetry() {
        Callable<Boolean> callable = () -> false;

        RetryConfig config = retryConfigBuilder
                .retryOnAnyException()
                .retryOnReturnValue(false)
                .withDelayBetweenTries(Duration.ZERO)
                .withMaxNumberOfTries(3)
                .withFixedBackoff()
                .build();

        assertRetryOccurs(config, callable);
    }

    @Test
    public void verifyRetryOnBooleanValue_shouldNotRetry() {
        Callable<Boolean> callable = () -> true;

        RetryConfig config = retryConfigBuilder
                .retryOnAnyException()
                .retryOnReturnValue(false)
                .withDelayBetweenTries(Duration.ZERO)
                .withMaxNumberOfTries(3)
                .withFixedBackoff()
                .build();

        assertRetryDoesNotOccur(config, callable);
    }

    @Test
    public void verifyRetryOnComplexValue_shouldRetry() {
        Callable<RetryOnValueTestObject> callable
                = () -> new RetryOnValueTestObject("should retry on this value!");

        RetryConfig config = retryConfigBuilder
                .retryOnAnyException()
                .retryOnReturnValue(new RetryOnValueTestObject("should retry on this value!"))
                .withDelayBetweenTries(Duration.ZERO)
                .withMaxNumberOfTries(3)
                .withFixedBackoff()
                .build();

        assertRetryOccurs(config, callable);
    }

    @Test
    public void verifyRetryOnComplexValue_shouldNotRetry() {
        Callable<RetryOnValueTestObject> callable
                = () -> new RetryOnValueTestObject("should NOT retry on this value!");

        RetryConfig config = retryConfigBuilder
                .retryOnAnyException()
                .retryOnReturnValue(new RetryOnValueTestObject("should retry on this value!"))
                .withDelayBetweenTries(Duration.ZERO)
                .withMaxNumberOfTries(3)
                .withFixedBackoff()
                .build();

        assertRetryDoesNotOccur(config, callable);
    }

    @Test
    public void verifyRetryOnValueAndExceptionInSameCall() {
        final Random random = new Random();
        Callable<Boolean> callable = () -> {
            if (random.nextBoolean()) {
                return false;
            } else {
                throw new FileNotFoundException();
            }
        };

        RetryConfig config = retryConfigBuilder
                .retryOnSpecificExceptions(FileNotFoundException.class)
                .retryOnReturnValue(false)
                .withDelayBetweenTries(Duration.ZERO)
                .withMaxNumberOfTries(100)
                .withFixedBackoff()
                .build();

        assertRetryOccurs(config, callable, 100);
    }

    private void assertRetryOccurs(RetryConfig config, Callable<?> callable, int expectedNumberOfTries) {
        try {
            new CallExecutor(config).execute(callable);
            fail("Expected RetriesExhaustedException but one wasn't thrown!");
        } catch (RetriesExhaustedException e) {
            assertThat(e.getCallResults().wasSuccessful()).isFalse();
            assertThat(e.getCallResults().getTotalTries()).isEqualTo(expectedNumberOfTries);
        }
    }

    private void assertRetryOccurs(RetryConfig config, Callable<?> callable) {
        assertRetryOccurs(config, callable, 3);
    }

    private void assertRetryDoesNotOccur(RetryConfig config, Callable<?> callable) {
        CallResults results = new CallExecutor(config).execute(callable);
        assertThat(results.wasSuccessful()).isTrue();
        assertThat(results.getTotalTries()).isEqualTo(1);
    }

    private class RetryOnValueTestObject {

        private String blah;

        RetryOnValueTestObject(String blah) {
            this.blah = blah;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            RetryOnValueTestObject that = (RetryOnValueTestObject) o;

            return blah != null ? blah.equals(that.blah) : that.blah == null;
        }

        @Override
        public int hashCode() {
            return blah != null ? blah.hashCode() : 0;
        }
    }

}
