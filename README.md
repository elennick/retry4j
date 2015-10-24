## Synopsis

retry4j is a simple Java library to assist with retrying on transient or unreliable logic.

## Basic Code Example

    Callable<Boolean> callable = () -> {
        //some code that you want to retry until success or retries are exhausted
    };

    RetryConfig retryConfig = new RetryConfigBuilder()
            .retryOnSpecificExceptions(ConnectException.class, TimeoutException.class)
            .withMaxNumberOfTries(10)
            .withDurationBetweenTries(Duration.of(30, ChronoUnit.SECONDS))
            .withExponentialBackoff()
            .build();
            
    try {  
      RetryResults results = new RetryExecutor(retryConfig).execute(callable);
    } catch(CallFailureException cfe) {
      //the call exhausted all tries without succeeding
    } catch(UnexpectedCallFailureException ucfe) {
      //the call threw an unexpected exception that was not specified to retry on
    }

## Motivation

There are several libraries that have similar capabilities this but I found them to either not work as advertised, to be overly complex or to be poorly documented. retry4j aims to be simple, readable and to be well documented.

## Dependency

TBD

## Requirements

retry4j does not require any external dependencies. It does require that you are using Java 8 or newer.
