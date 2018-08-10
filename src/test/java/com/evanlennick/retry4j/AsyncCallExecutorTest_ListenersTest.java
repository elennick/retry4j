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

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);

        RetryConfig config = new RetryConfigBuilder()
                .retryOnAnyException()
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();

        executor = new AsyncCallExecutor<>(config, Executors.newFixedThreadPool(5));
    }

    @Test
    public void verifyOnListener_resultHasTypeOfCallExecutor() throws Exception {
        List<String> methodCalls = new ArrayList<>();
        executor
                .onSuccess(status -> {
                    methodCalls.add("onSuccess");
                    assertThat(status.getResult()).isInstanceOf(String.class);
                })
                .onCompletion(status -> {
                    methodCalls.add("onCompletion");
                    assertThat(status.getResult()).isInstanceOf(String.class);
                })
                .execute(() -> "").get();
        assertThat(methodCalls).contains("onSuccess", "onCompletion");
    }
}
