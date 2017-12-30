package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
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

    private void assertRetryOccurs(RetryConfig config, Callable<?> callable) {
        try {
            new CallExecutor(config).execute(callable);
            fail("Expected RetriesExhaustedException but one wasn't thrown!");
        } catch (RetriesExhaustedException e) {
            assertThat(e.getCallResults().wasSuccessful()).isFalse();
            assertThat(e.getCallResults().getTotalTries()).isEqualTo(3);
        }
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
