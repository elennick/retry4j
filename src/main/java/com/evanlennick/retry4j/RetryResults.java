package com.evanlennick.retry4j;

import java.time.Duration;

public class RetryResults {

    private String callName;
    private boolean succeeded;
    private int totalTries;
    private Duration totalDurationElapsed;

    public String getCallName() {
        return callName;
    }

    public void setCallName(String callName) {
        this.callName = callName;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public int getTotalTries() {
        return totalTries;
    }

    public void setTotalTries(int totalTries) {
        this.totalTries = totalTries;
    }

    public Duration getTotalDurationElapsed() {
        return totalDurationElapsed;
    }

    public void setTotalDurationElapsed(Duration totalDurationElapsed) {
        this.totalDurationElapsed = totalDurationElapsed;
    }

    @Override
    public String toString() {
        return "RetryResults{" +
                "callName='" + callName + '\'' +
                ", succeeded=" + succeeded +
                ", totalTries=" + totalTries +
                ", totalDurationElapsed=" + totalDurationElapsed +
                '}';
    }
}
