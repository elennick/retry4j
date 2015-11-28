package com.evanlennick.retry4j.listener;

import com.evanlennick.retry4j.CallResults;

public interface OnSuccessListener extends RetryListener {

    void onSuccess(CallResults callResults);

}
