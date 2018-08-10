package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.evanlennick.retry4j.listener.RetryListener;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AsyncCallExecutorTest_ListenersTest {
    @Mock
    private DummyMock dummyMock;
    private AsyncCallExecutor<String> executor;
    private Callable<String> callable;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);

        callable = () -> dummyMock.callableCallThis();

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
        when(dummyMock.callableCallThis())
                .thenReturn("success!");

        RetryListener<String> listener = status -> {
            dummyMock.listenersCallThis();
            assertThat(status.getResult()).isInstanceOf(String.class);
        };
        executor
                .onSuccess(listener)
                .onCompletion(listener)
                .execute(callable).get();

        // ensure both listeners are called
        verify(dummyMock, timeout(1000).times(2)).listenersCallThis();
    }

    private class DummyMock {
        public String listenersCallThis() {
            return "this is to use to verify listeners call the mock";
        }

        public String listenersCallThis(Exception e) {
            return "this is to verify exceptions in the after failed call listener";
        }

        public String callableCallThis() {
            return "this is to use for mocking the executed callable";
        }
    }
}
