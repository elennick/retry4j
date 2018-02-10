package com.evanlennick.retry4j.exception;

public class Retry4jException extends RuntimeException {

    public Retry4jException(String message, Throwable cause) {
        super(message, cause);
    }

    public Retry4jException(String message) {
        super(message);
    }

}
