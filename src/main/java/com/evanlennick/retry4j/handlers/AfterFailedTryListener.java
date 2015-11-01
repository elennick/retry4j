package com.evanlennick.retry4j.handlers;

import com.evanlennick.retry4j.CallResults;

public interface AfterFailedTryListener extends RetryListener {

    abstract public void immediatelyAfterFailedTry(CallResults results);

}
