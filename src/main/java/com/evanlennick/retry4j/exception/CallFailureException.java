package com.evanlennick.retry4j.exception;

import com.evanlennick.retry4j.RetryResults;

import java.util.concurrent.ExecutionException;

/**
 * This exception represents a call execution that never succeeded after exhausting all retries.
 */
public class CallFailureException extends ExecutionException {

    private RetryResults results;

    public CallFailureException(RetryResults results) {
        super();
        this.results = results;
    }

    public CallFailureException(String message, RetryResults results) {
        super(message);
        this.results = results;
    }

    public RetryResults getRetryResults() {
        return results;
    }
}
