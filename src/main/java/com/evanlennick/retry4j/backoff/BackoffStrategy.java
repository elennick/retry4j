package com.evanlennick.retry4j.backoff;

import java.time.Duration;

public interface BackoffStrategy {

    abstract public long getMillisToWait(int numberOfTriesFailed, Duration delayBetweenAttempts);

}
