package com.evanlennick.retry4j.exception;

public class Retry4jException extends RuntimeException {

    private String message;

    public Retry4jException(String message) {
        this.message = message;
    }

    public Retry4jException() {

    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
