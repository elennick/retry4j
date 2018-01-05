package com.evanlennick.retry4j.exception;

import com.evanlennick.retry4j.Status;

/**
 * This exception represents a call execution that never succeeded after exhausting all retries.
 */
public class RetriesExhaustedException extends Retry4jException {

    private Status status;

    public RetriesExhaustedException(Status results) {
        super();
        this.status = results;
    }

    public RetriesExhaustedException(String message, Status status) {
        super(message);
        this.status = status;
    }

    public RetriesExhaustedException(String message, Throwable cause, Status status) {
        super(message, cause);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
