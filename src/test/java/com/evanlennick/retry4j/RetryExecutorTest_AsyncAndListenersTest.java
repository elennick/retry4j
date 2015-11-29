package com.evanlennick.retry4j;

import com.evanlennick.retry4j.listener.AfterFailedTryListener;
import com.evanlennick.retry4j.listener.BeforeNextTryListener;
import com.evanlennick.retry4j.listener.OnFailureListener;
import com.evanlennick.retry4j.listener.OnSuccessListener;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;

import static org.mockito.Mockito.*;

public class RetryExecutorTest_AsyncAndListenersTest {

    @Mock
    private DummyMock dummyMock;

    private CallExecutor executor;

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

        executor = new CallExecutor(config);
    }

    @Test
    public void verifyAfterFailedListener() {
        when(dummyMock.callableCallThis())
                .thenThrow(new RuntimeException())
                .thenThrow(new RuntimeException())
                .thenReturn("success!");

        executor.registerRetryListener((AfterFailedTryListener) results -> dummyMock.listenersCallThis());
        executor.execute(callable);

        verify(dummyMock, timeout(1000).times(2)).listenersCallThis();
    }

    @Test
    public void verifyBeforeNextTryListener() {
        when(dummyMock.callableCallThis())
                .thenThrow(new RuntimeException())
                .thenThrow(new RuntimeException())
                .thenReturn("success!");

        executor.registerRetryListener((BeforeNextTryListener) results -> dummyMock.listenersCallThis());
        executor.execute(callable);

        verify(dummyMock, timeout(1000).times(2)).listenersCallThis();
    }

    @Test
    public void verifyOnSuccessListener() {
        when(dummyMock.callableCallThis())
                .thenThrow(new RuntimeException())
                .thenThrow(new RuntimeException())
                .thenThrow(new RuntimeException())
                .thenReturn("success!");

        executor.registerRetryListener((OnSuccessListener) results -> dummyMock.listenersCallThis());
        executor.execute(callable);

        verify(dummyMock, timeout(1000).times(1)).listenersCallThis();
    }

    @Test
    public void verifyOnFailureListener() {
        when(dummyMock.callableCallThis())
                .thenThrow(new RuntimeException());

        executor.registerRetryListener((OnFailureListener) results -> dummyMock.listenersCallThis());
        executor.execute(callable);

        verify(dummyMock, timeout(1000).times(1)).listenersCallThis();
    }

    private class DummyMock {
        public String listenersCallThis() {
            return "this is to use to verify listeners call the mock";
        }

        public String callableCallThis() {
            return "this is to use for mocking the executed callable";
        }
    }
}
