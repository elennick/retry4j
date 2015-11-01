package com.evanlennick.retry4j.exception;

import java.util.concurrent.ExecutionException;

/**
 * This exception represents when a call throws an exception that was not specified as one to retry on in the RetryConfig.
 */
public class UnexpectedException extends ExecutionException {
    protected UnexpectedException() {
        super();
    }

    protected UnexpectedException(String message) {
        super(message);
    }

    public UnexpectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedException(Throwable cause) {
        super(cause);
    }
}
