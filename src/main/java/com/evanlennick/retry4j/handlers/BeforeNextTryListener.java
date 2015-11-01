package com.evanlennick.retry4j.handlers;

import com.evanlennick.retry4j.CallResults;

public interface BeforeNextTryListener extends RetryListener {

    abstract public void immediatelyBeforeNextTry(CallResults results);

}
