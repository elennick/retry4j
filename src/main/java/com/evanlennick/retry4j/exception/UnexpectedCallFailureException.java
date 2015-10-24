package com.evanlennick.retry4j.exception;

import java.util.concurrent.ExecutionException;

/**
 * This exception represents when a call throws an exception that was not specified as one to retry on in the RetryConfig.
 */
public class UnexpectedCallFailureException extends ExecutionException {
    protected UnexpectedCallFailureException() {
        super();
    }

    protected UnexpectedCallFailureException(String message) {
        super(message);
    }

    public UnexpectedCallFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedCallFailureException(Throwable cause) {
        super(cause);
    }
}
