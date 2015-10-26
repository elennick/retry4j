package com.evanlennick.retry4j;

import com.evanlennick.retry4j.exception.CallFailureException;
import com.evanlennick.retry4j.exception.UnexpectedCallFailureException;
import com.evanlennick.retry4j.handlers.RetryListener;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;

public class RetryListenerTest {

    @Test
    public void testRetryListener_immediatelyAfterTryFailed() throws UnexpectedCallFailureException, CallFailureException {
        Callable<Boolean> callable = () -> false;

        RetryExecutor executor = new RetryExecutor(RetryConfig.simpleFixedConfig());

        executor.registerRetryListener(new RetryListener() {
            @Override
            public void immediatelyAfterFailedTry(RetryResults results) {
                System.out.println("results = " + results);
            }

            @Override
            public void immediatelyBeforeNextTry(RetryResults results) {
                System.out.println("results = " + results);
            }
        });

        executor.execute(callable);
    }
}
