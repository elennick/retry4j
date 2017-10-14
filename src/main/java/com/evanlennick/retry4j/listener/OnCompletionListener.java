package com.evanlennick.retry4j.listener;

import com.evanlennick.retry4j.CallResults;

public interface OnCompletionListener extends RetryListener {

    void onCompletion(CallResults callResults);
}
