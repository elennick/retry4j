package com.evanlennick.retry4j.listener;

import com.evanlennick.retry4j.CallResults;

public interface OnFailureListener extends RetryListener {

    void onFailure(CallResults callResults);

}
