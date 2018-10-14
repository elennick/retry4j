package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CallExecutorTest_ListenersTest {

    @Mock
    private DummyMock dummyMock;

    private Callable<String> callable;
    private RetryConfig config;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);

        callable = () -> dummyMock.callableCallThis();

        config = new RetryConfigBuilder()
                .retryOnAnyException()
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(0, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();
    }

    @Test
    public void verifyAfterFailedListener() {
        when(dummyMock.callableCallThis())
                .thenThrow(new RuntimeException())
                .thenThrow(new RuntimeException())
                .thenReturn("success!");

        new CallExecutorBuilder().config(config)
                .afterFailedTryListener(status -> dummyMock.listenersCallThis())
                .build()
                .execute(callable);

        verify(dummyMock, timeout(1000).times(2)).listenersCallThis();
    }

    @Test
    public void verifyAfterFailedListener_populatesException() {
        when(dummyMock.callableCallThis())
                .thenThrow(new IllegalArgumentException())
                .thenReturn("success!");

        new CallExecutorBuilder().config(config)
                .afterFailedTryListener(status -> dummyMock.listenersCallThis(status.getLastExceptionThatCausedRetry()))
                .build()
                .execute(callable);

        verify(dummyMock, timeout(1000).times(1)).listenersCallThis(isA(IllegalArgumentException.class));
    }

    @Test
    public void verifyBeforeNextTryListener() {
        when(dummyMock.callableCallThis())
                .thenThrow(new RuntimeException())
                .thenThrow(new RuntimeException())
                .thenReturn("success!");

        new CallExecutorBuilder().config(config)
                .beforeNextTryListener(status -> dummyMock.listenersCallThis())
                .build()
                .execute(callable);

        verify(dummyMock, timeout(1000).times(2)).listenersCallThis();
    }

    @Test
    public void verifyBeforeNextTryListener_NotCalledAfterLastFailure() {
        when(dummyMock.callableCallThis())
                .thenThrow(new NullPointerException())
                .thenThrow(new NullPointerException())
                .thenThrow(new NullPointerException())
                .thenThrow(new NullPointerException())
                .thenThrow(new IllegalArgumentException());

        CallExecutor executor = new CallExecutorBuilder().config(config)
                .beforeNextTryListener(status -> dummyMock.listenersCallThis(status.getLastExceptionThatCausedRetry()))
                .build();
        try {
            executor.execute(callable);
        } catch (Exception e) {}

        // the beforeNextTry listener should only be called before a retry (5 attempts == 4 retries)
        verify(dummyMock, timeout(1000).times(4)).listenersCallThis(isA(NullPointerException.class));
        verify(dummyMock, after(1000).never()).listenersCallThis(isA(IllegalArgumentException.class));
    }

    @Test
    public void verifyOnSuccessListener() {
        when(dummyMock.callableCallThis())
                .thenThrow(new RuntimeException())
                .thenThrow(new RuntimeException())
                .thenThrow(new RuntimeException())
                .thenReturn("success!");

        new CallExecutorBuilder().config(config)
                .onSuccessListener(status -> dummyMock.listenersCallThis())
                .build()
                .execute(callable);

        verify(dummyMock, timeout(1000).times(1)).listenersCallThis();
    }

    @Test
    public void verifyOnFailureListener() {
        when(dummyMock.callableCallThis())
                .thenThrow(new RuntimeException());

        new CallExecutorBuilder().config(config)
                .onFailureListener(status -> dummyMock.listenersCallThis())
                .build()
                .execute(callable);

        verify(dummyMock, timeout(1000).times(1)).listenersCallThis();
    }

    @Test
    public void verifyOnCompletionListener_isCalledAfterSuccess() {
        when(dummyMock.callableCallThis())
                .thenReturn("success!");

        new CallExecutorBuilder().config(config)
                .onCompletionListener(status -> dummyMock.listenersCallThis())
                .build()
                .execute(callable);

        verify(dummyMock, timeout(1000).times(1)).listenersCallThis();
    }

    @Test
    public void verifyOnCompletionListener_isCalledAfterFailure() {
        when(dummyMock.callableCallThis())
                .thenThrow(new RuntimeException());

        CallExecutor callExecutor = new CallExecutorBuilder().config(config)
                .onCompletionListener(status -> dummyMock.listenersCallThis())
                .build();
        try {
            callExecutor.execute(callable);
        } catch (Exception e) {}

        verify(dummyMock, timeout(1000).times(1)).listenersCallThis();
    }

    @Test
    public void verifyChainedListeners_successImmediately() {
        new CallExecutorBuilder().config(config)
                .onSuccessListener(status -> dummyMock.listenersCallThis())
                .onFailureListener(status -> dummyMock.listenersCallThis())
                .onCompletionListener(status -> dummyMock.listenersCallThis())
                .afterFailedTryListener(status -> dummyMock.listenersCallThis())
                .beforeNextTryListener(status -> dummyMock.listenersCallThis())
                .build()
                .execute(callable);

        //only success and completion should wind up being called
        verify(dummyMock, timeout(1000).times(2)).listenersCallThis();
    }

    @Test
    public void verifyChainedListeners_successAfterRetries() {
        when(dummyMock.callableCallThis())
                .thenThrow(new RuntimeException())
                .thenThrow(new RuntimeException())
                .thenThrow(new RuntimeException())
                .thenReturn("success!");

        new CallExecutorBuilder().config(config)
                .onSuccessListener(status -> dummyMock.listenersCallThis())
                .onFailureListener(status -> dummyMock.listenersCallThis())
                .onCompletionListener(status -> dummyMock.listenersCallThis())
                .afterFailedTryListener(status -> dummyMock.listenersCallThis())
                .beforeNextTryListener(status -> dummyMock.listenersCallThis())
                .build()
                .execute(callable);

        //only calls success once, completion once and the retry listeners 3 times each
        verify(dummyMock, timeout(1000).times(8)).listenersCallThis();
    }

    @Test
    public void verifyOnFailureListener_populatesException() {
        when(dummyMock.callableCallThis())
                .thenThrow(new RuntimeException())
                .thenThrow(new IllegalArgumentException());

        new CallExecutorBuilder().config(config)
                .onFailureListener(status -> dummyMock.listenersCallThis(status.getLastExceptionThatCausedRetry()))
                .build().execute(callable);

        verify(dummyMock, timeout(1000)).listenersCallThis(isA(IllegalArgumentException.class));
    }

    @Test
    public void verifyOnListener_resultHasTypeOfCallExecutor() {
        List<String> methodCalls = new ArrayList<>();
        new CallExecutorBuilder().config(config)
                .onSuccessListener(status -> {
                    methodCalls.add("onSuccess");
                    assertThat(status.getResult()).isInstanceOf(String.class);
                })
                .onCompletionListener(status -> {
                    methodCalls.add("onCompletion");
                    assertThat(status.getResult()).isInstanceOf(String.class);
                })
                .build()
                .execute(() -> "");
        assertThat(methodCalls).contains("onSuccess", "onCompletion");
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
