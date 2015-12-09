package com.evanlennick.retry4j;

import com.sun.istack.internal.Nullable;

import java.time.Duration;

public class CallResults<T> {

    private long startTime;
    private long endTime;
    private String callName;
    private boolean wasSuccessful;
    private int totalTries;
    private Duration totalElapsedDuration;
    private T result;
    private Exception lastExceptionThatCausedRetry;

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

    public @Nullable T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public @Nullable Exception getLastExceptionThatCausedRetry() {
        return lastExceptionThatCausedRetry;
    }

    public void setLastExceptionThatCausedRetry(Exception lastExceptionThatCausedRetry) {
        this.lastExceptionThatCausedRetry = lastExceptionThatCausedRetry;
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
                ", lastExceptionThatCausedRetry=" + lastExceptionThatCausedRetry +
                '}';
    }

}
