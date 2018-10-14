package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncCallExecutorTest_ListenersTest {

    private AsyncCallExecutor<String> executor;
    private RetryConfig config;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);

        config = new RetryConfigBuilder()
                .retryOnAnyException()
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();
    }

    @Test
    public void verifyOnListener_resultHasTypeOfCallExecutor() throws Exception {
        List<String> methodCalls = new ArrayList<>();
        new CallExecutorBuilder()
                .config(config)
                .onSuccessListener(status -> {
                    methodCalls.add("onSuccess");
                    assertThat(status.getResult()).isInstanceOf(String.class);
                })
                .onCompletionListener(status -> {
                    methodCalls.add("onCompletion");
                    assertThat(status.getResult()).isInstanceOf(String.class);
                })
                .buildAsync(Executors.newFixedThreadPool(5))
                .execute(() -> "").get();
        assertThat(methodCalls).contains("onSuccess", "onCompletion");
    }
}
