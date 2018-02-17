package com.evanlennick.retry4j;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class AttemptStatus<T> {

    @Getter
    @Setter
    private T result;

    @Setter
    private boolean wasSuccessful;

    public boolean wasSuccessful() {
        return wasSuccessful;
    }

}
