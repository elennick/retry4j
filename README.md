## Synopsis

Retry4j is a simple Java library to assist with retrying on transient or unreliable logic.

## Basic Code Example

    Callable<Boolean> callable = () -> {
        //code that you want to retry until success OR retries are exhausted OR an unexpected exception is thrown
    };

    RetryConfig retryConfig = new RetryConfigBuilder()
            .retryOnSpecificExceptions(ConnectException.class)
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

There are several libraries that have similar capabilities this but I found them to either not work as advertised, to be overly complex or to be poorly documented. Retry4j aims to be simple, readable and to be well documented.

## Dependency

TBD

## Requirements

Retry4j does not require any external dependencies. It does require that you are using Java 8 or newer.

## More Examples

### Exception Handling Config

If you do not specify how exceptions should be handled or explicitly say **failOnAnyException()**, the RetryExecutor will fail and throw an **UnexpectedCallFailureException**. Use this configuration if you want the executor to cease its work when it runs into any exception at all.

    RetryConfig retryConfig = new RetryConfigBuilder()
            .failOnAnyException()
            .build();

If you want to specify specific exceptions that should cause the executor to continue and retry on encountering, specify them using the **retryOnSpecificExceptions()** config method. This method can accept any number of exceptions if there is more than one that should indicate the executor should continue retrying. All other unspecified exceptions will immediately interupt the executor and throw an **UnexpectedCallFailureException**.

    RetryConfig retryConfig = new RetryConfigBuilder()
            .retryOnSpecificExceptions(ConnectException.class, TimeoutException.class)
            .build();

If you want the executor to continue to retry on all encountered exceptions, specify this using the **retryOnAnyException()** config option.

    RetryConfig retryConfig = new RetryConfigBuilder()
            .retryOnAnyException()
            .build();

### Timing Config

To specify the maximum number of tries that should be attempted, specify an integer value in the config using the **withMaxNumberOfTries()** method. The executor will attempt to execute the call the number of times specified and if it does not succeed after all tries have been exhausted, it will throw a **CallFailureException**.

    RetryConfig retryConfig = new RetryConfigBuilder()
            .withMaxNumberOfTries(5)
            .build();

To specify the delay in between each try, use the **withDelayBetweenTries()** config method. This method will accept an integer value (specifying seconds), a long value (specifying milliseconds) or a Java 8 Duration object.

    //5 seconds
    RetryConfig retryConfig = new RetryConfigBuilder()
            .withDelayBetweenTries(5)
            .build();

    //500 millis
    RetryConfig retryConfig = new RetryConfigBuilder()
            .withDelayBetweenTries(500L)
            .build();

    //2 minutes, using Java 8 Duration
    RetryConfig retryConfig = new RetryConfigBuilder()
            .withDelayBetweenTries(Duration.of(2, ChronoUnit.MINUTES))
            .build();
            

### Config Backoff Strategy

Retry4j supports several backoff strategies. They can be specified like so:

    //backoff strategy that delays with the same interval in between every try
    RetryConfig retryConfig = new RetryConfigBuilder()
            .withFixedBackoff()
            .build();

    //backoff strategy that delays at a slowing rate using an exponential approach
    RetryConfig retryConfig = new RetryConfigBuilder()
            .withExponentialBackoff()
            .build();

    //backoff strategy that delays at a slowing rate using fibonacci numbers
    RetryConfig retryConfig = new RetryConfigBuilder()
            .withFibonacciBackoff()
            .build();

Backoff strategies can also be specified like so:

    RetryConfig retryConfig = new RetryConfigBuilder()
            .withBackoffStrategy(new FixedBackoffStrategy())
            .build();

Additionally, this config method can be used to specify custom backoff strategies. Custom backoff strategies can be created by implementing the **com.evanlennick.retry4j.backoff.BackoffStrategy** interface.
