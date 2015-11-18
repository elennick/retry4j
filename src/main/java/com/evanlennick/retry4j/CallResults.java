package com.evanlennick.retry4j;

import java.time.Duration;

public class CallResults<T> {

    private long startTime;
    private long endTime;
    private String callName;
    private boolean wasSuccessful;
    private int totalTries;
    private Duration totalElapsedDuration;
    private T result;

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

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "CallResults{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", callName='" + callName + '\'' +
                ", wasSuccessful=" + wasSuccessful +
                ", totalTries=" + totalTries +
                ", totalElapsedDuration=" + totalElapsedDuration +
                ", result=" + result +
                '}';
    }

}
