package com.evanlennick.retry4j;

class CustomTestException extends RuntimeException {

    private int someValue;

    public CustomTestException(String message, int someValue) {
        super(message);
        this.someValue = someValue;
    }

    public int getSomeValue() {
        return someValue;
    }
}
