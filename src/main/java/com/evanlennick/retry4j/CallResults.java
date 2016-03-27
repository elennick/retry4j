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

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Exception getLastExceptionThatCausedRetry() {
        return lastExceptionThatCausedRetry;
    }

    public void setLastExceptionThatCausedRetry(Exception lastExceptionThatCausedRetry) {
        this.lastExceptionThatCausedRetry = lastExceptionThatCausedRetry;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallResults{");
        sb.append("startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", callName='").append(callName).append('\'');
        sb.append(", wasSuccessful=").append(wasSuccessful);
        sb.append(", totalTries=").append(totalTries);
        sb.append(", totalElapsedDuration=").append(totalElapsedDuration);
        sb.append(", result=").append(result);
        sb.append(", lastExceptionThatCausedRetry=").append(lastExceptionThatCausedRetry);
        sb.append('}');
        return sb.toString();
    }

}
