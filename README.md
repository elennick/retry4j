## Synopsis

Retry4j is a simple Java library to assist with retrying transient situations or unreliable code. Code that relies on connecting to an external resource that is intermittently available (ie: a REST API or external database connection) is a good example of where this type of logic is useful.

## Basic Code Example

    Callable<Boolean> callable = () -> {
        //code that you want to retry until success OR retries are exhausted OR an unexpected exception is thrown
    };

    RetryConfig config = new RetryConfigBuilder()
            .retryOnSpecificExceptions(ConnectException.class)
            .withMaxNumberOfTries(10)
            .withDurationBetweenTries(Duration.of(30, ChronoUnit.SECONDS))
            .withExponentialBackoff()
            .build();
            
    try {  
      RetryResults results = new RetryExecutor(config).execute(callable);
    } catch(CallFailureException cfe) {
      //the call exhausted all tries without succeeding
    } catch(UnexpectedCallFailureException ucfe) {
      //the call threw an unexpected exception that was not specified to retry on
    }

## Motivation

There are several libraries that have similar capabilities this but I found them to either not work as advertised, to be overly complex or to be poorly documented. Retry4j aims to be simple, readable and to be well documented.

## Dependency

TBD

## Documentation

### General

Retry4j currently supports simple calls that do not return any complex logic that needs to be handled. It only supports synchronous requests and does not handle threading or asynchronous callbacks for you. Retry4j does not require any external dependencies. It does require that you are using Java 8 or newer.

### Exception Handling Config

If you do not specify how exceptions should be handled or explicitly say **failOnAnyException()**, the RetryExecutor will fail and throw an **UnexpectedCallFailureException**. Use this configuration if you want the executor to cease its work when it runs into any exception at all.

    RetryConfig config = new RetryConfigBuilder()
            .failOnAnyException()
            .build();

If you want to specify specific exceptions that should cause the executor to continue and retry on encountering, specify them using the **retryOnSpecificExceptions()** config method. This method can accept any number of exceptions if there is more than one that should indicate the executor should continue retrying. All other unspecified exceptions will immediately interupt the executor and throw an **UnexpectedCallFailureException**.

    RetryConfig config = new RetryConfigBuilder()
            .retryOnSpecificExceptions(ConnectException.class, TimeoutException.class)
            .build();

If you want the executor to continue to retry on all encountered exceptions, specify this using the **retryOnAnyException()** config option.

    RetryConfig config = new RetryConfigBuilder()
            .retryOnAnyException()
            .build();

### Timing Config

To specify the maximum number of tries that should be attempted, specify an integer value in the config using the **withMaxNumberOfTries()** method. The executor will attempt to execute the call the number of times specified and if it does not succeed after all tries have been exhausted, it will throw a **CallFailureException**.

    RetryConfig config = new RetryConfigBuilder()
            .withMaxNumberOfTries(5)
            .build();

To specify the delay in between each try, use the **withDelayBetweenTries()** config method. This method will accept an integer value (specifying seconds), a long value (specifying milliseconds) or a Java 8 Duration object.

    //5 seconds
    RetryConfig config = new RetryConfigBuilder()
            .withDelayBetweenTries(5)
            .build();

    //500 millis
    RetryConfig config = new RetryConfigBuilder()
            .withDelayBetweenTries(500L)
            .build();

    //2 minutes, using Java 8 Duration
    RetryConfig config = new RetryConfigBuilder()
            .withDelayBetweenTries(Duration.of(2, ChronoUnit.MINUTES))
            .build();
            

### Config Backoff Strategy

Retry4j supports several backoff strategies. They can be specified like so:

    //backoff strategy that delays with the same interval in between every try
    RetryConfig config = new RetryConfigBuilder()
            .withFixedBackoff()
            .build();

    //backoff strategy that delays at a slowing rate using an exponential approach
    RetryConfig config = new RetryConfigBuilder()
            .withExponentialBackoff()
            .build();

    //backoff strategy that delays at a slowing rate using fibonacci numbers
    RetryConfig config = new RetryConfigBuilder()
            .withFibonacciBackoff()
            .build();

Backoff strategies can also be specified like so:

    RetryConfig config = new RetryConfigBuilder()
            .withBackoffStrategy(new FixedBackoffStrategy())
            .build();

Additionally, this config method can be used to specify custom backoff strategies. Custom backoff strategies can be created by implementing the **com.evanlennick.retry4j.backoff.BackoffStrategy** interface.

### RetryExecutor

Executing your code with retry logic is as simple as instantiating a **RetryExecutor** with your configuration and then calling execute:

    new RetryExecutor(config).execute(callable);
    
The RetryExecutor expects that your logic is wrapped in a **java.util.concurrent.Callable<Boolean>**.

### Retry Results

After the executor successfully completes or throws a CallFailureException, a **RetryResults** object will returned or included in the exception. This object will contain detailed information about the call execution including the number of total tries, the total elapsed time and whether or not the execution was considered successful upon completion.

    RetryResults results = new RetryExecutor(config).execute(callable);
    System.out.println(results.isSucceeded());
    System.out.println(results.getCallName());
    System.out.println(results.getTotalDurationElapsed());
    System.out.println(results.getTotalTries());
    
or

    try {  
        RetryResults results = new RetryExecutor(config).execute(callable);
    } catch(CallFailureException cfe) {
        RetryResults results = cfe.getRetryResults();
        System.out.println(results.isSucceeded());
        System.out.println(results.getCallName());
        System.out.println(results.getTotalDurationElapsed());
        System.out.println(results.getTotalTries());
    }

### What situations will the executor retry on?

If the call does not return true OR throws an expected exception (one specified to retry on by the config) AND and all retries have not yet been exhausted, the executor will delay and retry again.

### What situations will the executor throw a CallFailureException?

If the call does not return true OR throws an expected exception AND all retries have been exhausted, the executor will throw a CallFailureException.

### What situations will the executor throw an UnexpectedCallFailureException

If the call throws an unexpected exception, the executor will immediately stop and throw an UnexpectedCallFailureException. For example, if this is the config:

    RetryConfig config = new RetryConfigBuilder()
            .retryOnSpecificExceptions(ConnectException.class)
            .build();

...and an **UnsupportedOperationException** is thrown, the executor will bubble up the exception and cease to execute.

### Putting it all together with more realistic config examples

This example will execute the callable code up to three times with a delay of 5 seconds in between each try if needed. It will retry whenever the callable code does not return true OR when an IllegalArgumentException is thrown:

    RetryConfig config = new RetryConfigBuilder()
        .retryOnSpecificExceptions(IllegalArgumentException.class)
        .withMaxNumberOfTries(3)
        .withDelayBetweenTries(5)
        .withFixedBackoff()
        .build();

    new RetryExecutor(config).execute(callable);

This example will execute the callable code up to five times. It will initially delay for 500 milliseconds before trying again and each subsequent delay will become exponentially longer. Any encountered exceptions will be considered a failure for that try but will not stop the retry executor from continuing. After 5 failed tries, the executor will throw a CallFailureException:

    RetryConfig config = new RetryConfigBuilder()
        .retryOnAnyException()
        .withMaxNumberOfTries(5)
        .withDelayBetweenTries(Duration.of(500, ChronoUnit.MILLIS))
        .withExponentialBackoff()
        .build();

    new RetryExecutor(config).execute(callable);
