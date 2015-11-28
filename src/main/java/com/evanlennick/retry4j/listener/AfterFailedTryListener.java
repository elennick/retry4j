package com.evanlennick.retry4j.listener;

import com.evanlennick.retry4j.CallResults;

public interface AfterFailedTryListener extends RetryListener {

    void immediatelyAfterFailedTry(CallResults results);

}
