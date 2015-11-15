package com.evanlennick.retry4j.exception;

public class InvalidRetryConfigException extends Retry4jException {

    public InvalidRetryConfigException(String message) {
        super(message);
    }
}
