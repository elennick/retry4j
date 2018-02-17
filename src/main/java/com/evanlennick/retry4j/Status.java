package com.evanlennick.retry4j;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Duration;

@ToString(callSuper = true)
@Getter
@Setter
public class Status<T> extends AttemptStatus<T> {

    private String id;
    private long startTime;
    private long endTime;
    private String callName;
    private int totalTries;
    private Duration totalElapsedDuration;
    private Exception lastExceptionThatCausedRetry;

}
