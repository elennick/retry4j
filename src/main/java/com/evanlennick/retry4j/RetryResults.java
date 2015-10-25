package com.evanlennick.retry4j;

import java.time.Duration;

public class RetryResults {

    private String callName;
    private boolean wasSuccessful;
    private int totalTries;
    private Duration totalElapsedDuration;

    public String getCallName() {
        return callName;
    }

    public void setCallName(String callName) {
        this.callName = callName;
    }

    public boolean wasSuccessful() {
        return wasSuccessful;
    }

    public void setSuccessful(boolean wasSuccessful) {
        this.wasSuccessful = wasSuccessful;
    }

    public int getTotalTries() {
        return totalTries;
    }

    public void setTotalTries(int totalTries) {
        this.totalTries = totalTries;
    }

    @Override
    public String toString() {
        return "RetryResults{" +
                "callName='" + callName + '\'' +
                ", wasSuccessful=" + wasSuccessful +
                ", totalTries=" + totalTries +
                ", totalElapsedDuration=" + totalElapsedDuration +
                '}';
    }

    public boolean isWasSuccessful() {
        return wasSuccessful;
    }

    public void setWasSuccessful(boolean wasSuccessful) {
        this.wasSuccessful = wasSuccessful;
    }

    public Duration getTotalElapsedDuration() {
        return totalElapsedDuration;
    }

    public void setTotalElapsedDuration(Duration totalElapsedDuration) {
        this.totalElapsedDuration = totalElapsedDuration;
    }

}
