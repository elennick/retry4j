package com.evanlennick.retry4j.config;

import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.Status;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

public class RetryConfigBuilderTest_SimpleDefaultsTest {

    private RetryConfigBuilder retryConfigBuilder;

    @BeforeMethod
    public void setup() {
        retryConfigBuilder = new RetryConfigBuilder();
        retryConfigBuilder.setValidationEnabled(true);
    }

    @Test
    public void verifySimpleExponentialProfile() {
        Callable<Boolean> callable = () -> true;

        RetryConfig config = retryConfigBuilder
                .exponentialBackoff5Tries5Sec()
                .build();

        Status results = CallExecutor.<Boolean>builder()
                .withConfig(config)
                .build()
                .execute(callable);

        assertThat(results.wasSuccessful()).isTrue();
    }

    @Test
    public void verifySimpleFibonacciProfile() {
        Callable<Boolean> callable = () -> true;

        RetryConfig config = retryConfigBuilder
                .fiboBackoff7Tries5Sec()
                .build();

        Status results = CallExecutor.<Boolean>builder()
                .withConfig(config)
                .build()
                .execute(callable);

        assertThat(results.wasSuccessful()).isTrue();
    }

    @Test
    public void verifySimpleRandomExponentialProfile() {
        Callable<Boolean> callable = () -> true;

        RetryConfig config = retryConfigBuilder
                .randomExpBackoff10Tries60Sec()
                .build();

        Status results = CallExecutor.<Boolean>builder()
                .withConfig(config)
                .build()
                .execute(callable);

        assertThat(results.wasSuccessful()).isTrue();
    }

    @Test
    public void verifySimpleFixedProfile() {
        Callable<Boolean> callable = () -> true;

        RetryConfig config = retryConfigBuilder
                .fixedBackoff5Tries10Sec()
                .build();

        Status results = CallExecutor.<Boolean>builder()
                .withConfig(config)
                .build()
                .execute(callable);

        assertThat(results.wasSuccessful()).isTrue();
    }
}
