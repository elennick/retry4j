package com.evanlennick.retry4j.exception;

import java.util.concurrent.ExecutionException;

/**
 * This exception represents a call execution that never succeeded after exhausting all retries.
 */
public class CallFailureException extends ExecutionException {

    public CallFailureException() {
        super();
    }

    public CallFailureException(String message) {
        super(message);
    }

    public CallFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public CallFailureException(Throwable cause) {
        super(cause);
    }
}
