package com.evanlennick.retry4j.config;

import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.CallExecutorBuilder;
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

        RetryConfig retryConfig = retryConfigBuilder
            .exponentialBackoff5Tries5Sec()
            .build();

        CallExecutor callExecutor = new CallExecutorBuilder().config(retryConfig).build();
        Status results = callExecutor.execute(callable);
        assertThat(results.wasSuccessful());
    }

    @Test
    public void verifySimpleFibonacciProfile() {
        Callable<Boolean> callable = () -> true;

        RetryConfig retryConfig = retryConfigBuilder
            .fiboBackoff7Tries5Sec()
            .build();

        CallExecutor callExecutor = new CallExecutorBuilder().config(retryConfig).build();
        Status results = callExecutor.execute(callable);
        assertThat(results.wasSuccessful());
    }

    @Test
    public void verifySimpleRandomExponentialProfile() {
        Callable<Boolean> callable = () -> true;

        RetryConfig retryConfig = retryConfigBuilder
            .randomExpBackoff10Tries60Sec()
            .build();

        CallExecutor callExecutor = new CallExecutorBuilder().config(retryConfig).build();
        Status results = callExecutor.execute(callable);
        assertThat(results.wasSuccessful());
    }

    @Test
    public void verifySimpleFixedProfile() {
        Callable<Boolean> callable = () -> true;

        RetryConfig retryConfig = retryConfigBuilder
            .fixedBackoff5Tries10Sec()
            .build();

        CallExecutor callExecutor = new CallExecutorBuilder().config(retryConfig).build();
        Status results = callExecutor.execute(callable);
        assertThat(results.wasSuccessful());
    }
}
