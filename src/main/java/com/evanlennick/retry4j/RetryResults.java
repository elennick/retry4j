package com.evanlennick.retry4j;

import java.time.Duration;

public class RetryResults {

    private long startTime;
    private long endTime;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

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

    @Override
    public String toString() {
        return "RetryResults{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", callName='" + callName + '\'' +
                ", wasSuccessful=" + wasSuccessful +
                ", totalTries=" + totalTries +
                ", totalElapsedDuration=" + totalElapsedDuration +
                '}';
    }

}
