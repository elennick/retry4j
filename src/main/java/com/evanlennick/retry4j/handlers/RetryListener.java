package com.evanlennick.retry4j.handlers;

import com.evanlennick.retry4j.RetryResults;

public interface RetryListener extends Listener {

    abstract public void immediatelyAfterFailedTry(RetryResults results);

    abstract public void immediatelyBeforeNextTry(RetryResults results);

}
