package com.evanlennick.retry4j.exception;

/**
 * This exception represents when a call throws an exception that was not specified as one to retry on in the
 * RetryConfig.
 */
public class UnexpectedException extends Retry4jException {

    public UnexpectedException(String message, Throwable cause) {
        super(message, cause);
    }

}
