![Build Status](https://travis-ci.org/elennick/retry4j.svg?branch=master) ![License](https://img.shields.io/packagist/l/doctrine/orm.svg)

## Retry4j

Retry4j is a simple Java library to assist with retrying transient situations or unreliable code. Code that relies on connecting to an external resource that is intermittently available (ie: a REST API or external database connection) is a good example of where this type of logic is useful.

## Basic Code Example

    Callable<Object> callable = () -> {
        //code that you want to retry until success OR retries are exhausted OR an unexpected exception is thrown
    };

    RetryConfig config = new RetryConfigBuilder()
            .retryOnSpecificExceptions(ConnectException.class)
            .withMaxNumberOfTries(10)
            .withDelayBetweenTries(Duration.of(30, ChronoUnit.SECONDS))
            .withExponentialBackoff()
            .build();
            
    try {  
      CallResults<Object> results = new CallExecutor(config).execute(callable);
      Object object = results.getResult(); //the 
    } catch(RetriesExhaustedException ree) {
      //the call exhausted all tries without succeeding
    } catch(UnexpectedException ue) {
      //the call threw an unexpected exception that was not specified to retry on
    }

Or even more simple using one of the predefined config options:

    Callable<Object> callable = () -> {
        //code that you want to retry until success OR retries are exhausted OR an unexpected exception is thrown
    };
    
    try {  
      CallExecutor executor = new CallExecutor(RetryConfig.simpleFixedConfig());
      CallResults<Object> results = executor.execute(callable);
    } catch(RetriesExhaustedException ree) {
      //the call exhausted all tries without succeeding
    } catch(UnexpectedException ue) {
      //the call threw an unexpected exception that was not specified to retry on
    }

## Dependencies

### Maven

    <dependency>
        <groupId>com.evanlennick</groupId>
        <artifactId>retry4j</artifactId>
        <version>0.2.0</version>
    </dependency>

### SBT

    libraryDependencies += "com.evanlennick" % "retry4j" % "0.2.0"

## Motivation

There are several libraries that have similar capabilities this but I found them to either not work as advertised, to be overly complex or to be poorly documented. Retry4j aims to be readable, well documented and streamlined.

## Documentation

### General

Retry4j only supports synchronous requests and does not handle threading or asynchronous callbacks for you. Retry4j does not require any external dependencies. It does require that you are using Java 8 or newer.

### Exception Handling Config

If you do not specify how exceptions should be handled or explicitly say **failOnAnyException()**, the CallExecutor will fail and throw an **UnexpectedException**. Use this configuration if you want the executor to cease its work when it runs into any exception at all.

    RetryConfig config = new RetryConfigBuilder()
            .failOnAnyException()
            .build();

If you want to specify specific exceptions that should cause the executor to continue and retry on encountering, specify them using the **retryOnSpecificExceptions()** config method. This method can accept any number of exceptions if there is more than one that should indicate the executor should continue retrying. All other unspecified exceptions will immediately interupt the executor and throw an **UnexpectedException**.

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

    //backoff strategy that retries with no delay
    //NOTE: any value specified in the config for "withDelayBetweenTries()" will be ignored if you use this strategy
    RetryConfig config = new RetryConfigBuilder()
            .withNoWaitBackoff()
            .build();

    //backoff strategy that randomly multiplies the delay specified on each retry
    //useful if you want to force multiple threads not to retry at the same rate
    RetryConfig config = new RetryConfigBuilder()
            .withRandomBackoff()
            .build();

    //backoff strategy that delays at a slowing rate and also randomly multiplies the delay
    //combination of Exponential Backoff Strategy and Random Backoff Strategy
    RetryConfig config = new RetryConfigBuilder()
            .withRandomExponentialBackoff()
            .build();

Backoff strategies can also be specified like so:

    RetryConfig config = new RetryConfigBuilder()
            .withBackoffStrategy(new FixedBackoffStrategy())
            .build();

Additionally, this config method can be used to specify custom backoff strategies. Custom backoff strategies can be created by implementing the **com.evanlennick.retry4j.backoff.BackoffStrategy** interface.

### Simple Configs

Retry4j offers a few "simple" configurations right out of the box if specifying all the details isn't important to you. These can be accessed as static members of RetryConfig like so:

      CallExecutor executor = new CallExecutor(RetryConfig.simpleFixedConfig());
      CallResults results = executor.execute(callable);

**simpleFixedConfig()**, **simpleExponentialConfig()** and **simpleFibonacciConfig()** are all available.

### CallExecutor

Executing your code with retry logic is as simple as instantiating a **CallExecutor** with your configuration and then calling execute:

    new CallExecutor(config).execute(callable);
    
The CallExecutor expects that your logic is wrapped in a **java.util.concurrent.Callable**.

### CallResults

After the executor successfully completes or throws a CallFailureException, a **CallResults** object will returned or included in the exception. This object will contain detailed information about the call execution including the number of total tries, the total elapsed time and whether or not the execution was considered successful upon completion.

    CallResults results = new CallExecutor(config).execute(callable);
    System.out.println(results.getResult()); //this will be populated if your callable returns a value
    System.out.println(results.isSucceeded());
    System.out.println(results.getCallName());
    System.out.println(results.getTotalDurationElapsed());
    System.out.println(results.getTotalTries());
    
or

    try {  
        new CallExecutor(config).execute(callable);
    } catch(RetriesExhaustedException cfe) {
        CallResults results = cfe.getCallResults();
        System.out.println(results.isSucceeded());
        System.out.println(results.getCallName());
        System.out.println(results.getTotalDurationElapsed());
        System.out.println(results.getTotalTries());
    }

### RetryListener

Two RetryListener's are offered in case you want to be able to add logic that will execute immediately after a failed try or immediately before the next retry (for example, you may want to log or output a statement when something is retrying). These listeners can be specified like so:

        CallExecutor executor = new CallExecutor(config);
        
        executor.registerRetryListener((AfterFailedTryListener) results -> {
            //whatever logic you want to execute after a failed try
        });
        
        executor.registerRetryListener((BeforeNextTryListener) results -> {
            //whatever logic you want to execute before the next try
        }
