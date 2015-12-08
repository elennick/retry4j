package com.evanlennick.retry4j;

import com.evanlennick.retry4j.listener.OnFailureListener;
import com.evanlennick.retry4j.listener.OnSuccessListener;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.concurrent.Callable;

import static java.time.temporal.ChronoUnit.MILLIS;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class AsyncCallExecutorTest {

    @Mock
    private DummyMock dummyMock;

    private RetryConfig config;

    private AsyncCallExecutor asyncCallExecutor;

    private OnSuccessListener onSuccessListener;

    private OnFailureListener onFailureListener;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);

        config = RetryConfigBuilder.newConfig()
                .retryOnAnyException()
                .withMaxNumberOfTries(5)
                .withDelayBetweenTries(1, MILLIS)
                .withFixedBackoff()
                .build();

        asyncCallExecutor = new AsyncCallExecutor();

        onSuccessListener = callResults -> dummyMock.onSuccessListenersCallThis();
        onFailureListener = callResults -> dummyMock.onFaliureListenersCallThis();
    }

    @Test
    public void verifySimpleAsyncCall_success() {
        Callable<Boolean> callable = () -> true;

        asyncCallExecutor.execute(callable, config, Arrays.asList(onSuccessListener, onFailureListener));

        verify(dummyMock, timeout(500).times(1)).onSuccessListenersCallThis();
        verify(dummyMock, never()).onFaliureListenersCallThis();
    }

    @Test
    public void verifySimpleAsyncCall_failure() {
        Callable<Boolean> callable = () -> {
            throw new RuntimeException();
        };

        asyncCallExecutor.execute(callable, config, Arrays.asList(onSuccessListener, onFailureListener));

        verify(dummyMock, never()).onSuccessListenersCallThis();
        verify(dummyMock, timeout(500).times(1)).onFaliureListenersCallThis();
    }

    private class DummyMock {
        public String onSuccessListenersCallThis() {
            return "this is to use to verify onSuccess listeners call the mock";
        }

        public String onFaliureListenersCallThis() {
            return "this is to use to verify onFailure listeners call the mock";
        }
    }
}
