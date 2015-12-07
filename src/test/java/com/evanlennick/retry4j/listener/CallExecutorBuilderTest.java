package com.evanlennick.retry4j.listener;

import com.evanlennick.retry4j.CallResults;
import com.evanlennick.retry4j.RetryConfig;
import com.evanlennick.retry4j.RetryConfigBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import org.testng.annotations.Test;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;

public class CallExecutorBuilderTest {

    @Test
    public void verifyReturningObjectFromCallExecutorBuilderSucceeds() throws Exception {
        Callable<Boolean> callable = () -> true;

        RetryConfig retryConfig = new RetryConfigBuilder()
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        CallResults<Boolean> results = CallExecutorBuilder.<Boolean>newSyncCall(callable).withConfig(retryConfig).execute();
        assertThat(results.wasSuccessful());
    }

}
