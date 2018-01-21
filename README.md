![Build Status](https://travis-ci.org/elennick/retry4j.svg?branch=master&link=https://travis-ci.org/elennick/retry4j&link=https://travis-ci.org/elennick/retry4j) [![Coverage Status](https://coveralls.io/repos/github/elennick/retry4j/badge.svg?branch=master)](https://coveralls.io/github/elennick/retry4j?branch=master) ![Maven Central](https://img.shields.io/maven-central/v/com.evanlennick/retry4j.svg) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Retry4j

Retry4j is a simple Java library to assist with retrying transient failure situations or unreliable code. Retry4j aims to be readable, well documented and streamlined.

## Table of Contents

* [Basic Code Examples](#basic-code-examples)
    * [Handling Results Normally](#handling-results-normally)
    * [Handling Results With Listeners](#handling-results-with-listeners)
* [Dependencies](#dependencies)
    * [Maven](#maven)
    * [SBT](#sbt)
    * [Gradle](#gradle)
* [Usage](#usage)
    * [General](#general)
    * [Exception Handling Config](#exception-handling-config)
    * [Value Handling Config](#value-handling-config)
    * [Timing Config](#timing-config)
    * [Backoff Strategy Config](#backoff-strategy-config)
    * [Simple Configs](#simple-configs)
    * [CallExecutor](#callexecutor)
    * [CallResults](#callresults)
    * [Retry4jException](#retry4jexception)
    * [Listeners](#listeners)
    * [Async Support](#async-support)
* [Other Notes](#other-notes)
      
## Basic Code Examples

### Handling Results Normally

    Callable<Object> callable = () -> {
        //code that you want to retry until success OR retries are exhausted OR an unexpected exception is thrown
    };

    RetryConfig config = new RetryConfigBuilder()
            .retryOnSpecificExceptions(ConnectException.class)
            .withMaxNumberOfTries(10)
            .withDelayBetweenTries(30, ChronoUnit.SECONDS)
            .withExponentialBackoff()
            .build();
            
    try {  
        CallResults<Object> results = new CallExecutor(config).execute(callable);
        Object object = results.getResult(); //the result of the callable logic, if it returns one
    } catch(RetriesExhaustedException ree) {
        //the call exhausted all tries without succeeding
    } catch(UnexpectedException ue) {
        //the call threw an unexpected exception
    }

Or more simple using one of the predefined config options and not checking exceptions:

    Callable<Object> callable = () -> {
        //code that you want to retry
    };

    RetryConfig config = new RetryConfigBuilder()
        .exponentialBackoff5Tries5Sec()
        .build();

    CallResults<Object> results = new CallExecutor(config).execute(callable);

### Handling Results With Listeners

    Callable<Object> callable = () -> {
        //code that you want to retry
    };

    RetryConfig config = new RetryConfigBuilder()
        .exponentialBackoff5Tries5Sec()
        .build();

    CallExecutor executor = new CallExecutor(config);
    
    executor.registerRetryListener((OnFailureListener) results -> { /** some code to execute on failure **/ });
    executor.registerRetryListener((OnSuccessListener) results -> { /** some code to execute on success **/ });
    executor.execute(callable);

## Dependencies

### Maven

    <dependency>
        <groupId>com.evanlennick</groupId>
        <artifactId>retry4j</artifactId>
        <version>0.10.0</version>
    </dependency>

### SBT

    libraryDependencies += "com.evanlennick" % "retry4j" % "0.10.0"

### Gradle

    compile "com.evanlennick:retry4j:0.10.0"

## Usage

### General

Retry4j does not require any external dependencies. It does require that you are using Java 8 or newer. Javadocs are 
hosted at http://www.javadoc.io/doc/com.evanlennick/retry4j/0.10.0.

Continuous Integration results are available via Travis CI here: https://travis-ci.org/elennick/retry4j.

### Exception Handling Config

If you do not specify how exceptions should be handled or explicitly say **failOnAnyException()**, the CallExecutor will fail and throw an **UnexpectedException** when encountering exceptions while running. Use this configuration if you want the executor to cease its work when it runs into any exception at all.

    RetryConfig config = new RetryConfigBuilder()
            .failOnAnyException()
            .build();

If you want to specify specific exceptions that should cause the executor to continue and retry on encountering, do so using the **retryOnSpecificExceptions()** config method. This method can accept any number of exceptions if there is more than one that should indicate the executor should continue retrying. All other unspecified exceptions will immediately interupt the executor and throw an **UnexpectedException**.

    RetryConfig config = new RetryConfigBuilder()
            .retryOnSpecificExceptions(ConnectException.class, TimeoutException.class)
            .build();

If you want the executor to continue to retry on all encountered exceptions, specify this using the **retryOnAnyException()** config option.

    RetryConfig config = new RetryConfigBuilder()
            .retryOnAnyException()
            .build();

If you want the executor to continue to retry on all encountered exceptions EXCEPT for a few specific ones, specify this using the **retryOnAnyExceptionExcluding()** config option. If this exception strategy is chosen, only the exceptions specified or their subclasses will interupt the executor and throw an **UnexpectedException**.

    RetryConfig config = new RetryConfigBuilder()
            .retryOnAnyExceptionExcluding(CriticalFailure.class, DontRetryOnThis.class)
            .build();
    
### Value Handling Config

If you want the executor to retry based on the returned value from the Callable:

    RetryConfig config = retryConfigBuilder
            .retryOnReturnValue("retry on this value!")
            .build();
                
This can be used in combination with exception handling configuration like so:

    RetryConfig config = retryConfigBuilder
            .retryOnSpecificExceptions(FileNotFoundException.class)
            .retryOnReturnValue("retry on this value!")
            .build();
            
In the above scenario, the call execution will be considered a failure and a retry will be triggered if 
`FileNotFoundException.class` is thrown OR if the String `retry on this value!` is returned from the Callable logic.

To retry only using return values, you can disable exception retries using the `failOnAnyException()` configuration option:

    RetryConfig config = retryConfigBuilder
            .failOnAnyException()
            .retryOnReturnValue("retry on this value!")
            .build();
        
### Timing Config

To specify the maximum number of tries that should be attempted, specify an integer value in the config using the **withMaxNumberOfTries()** method. The executor will attempt to execute the call the number of times specified and if it does not succeed after all tries have been exhausted, it will throw a **RetriesExhaustedException**.

    RetryConfig config = new RetryConfigBuilder()
            .withMaxNumberOfTries(5)
            .build();
            
If you do not wish to have a maximum and want retries to continue indefinitely, instead us the `retryIndefinitely()` 
option:

    RetryConfig config = new RetryConfigBuilder()
            .retryIndefinitely()
            .build();

To specify the delay in between each try, use the **withDelayBetweenTries()** config method. This method will accept a Java 8 Duration object or an integer combined with a ChronoUnit.

    //5 seconds
    RetryConfig config = new RetryConfigBuilder()
            .withDelayBetweenTries(5, ChronoUnit.SECONDS)
            .build();

    //2 minutes
    RetryConfig config = new RetryConfigBuilder()
            .withDelayBetweenTries(Duration.of(2, ChronoUnit.MINUTES))
            .build();

    //250 millis
    RetryConfig config = new RetryConfigBuilder()
            .withDelayBetweenTries(Duration.ofMillis(250))
            .build();

### Backoff Strategy Config

Retry4j has built in support for several backoff strategies. They can be specified like so:

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

Custom backoff strategies can also be specified like so:

    RetryConfig config = new RetryConfigBuilder()
            .withBackoffStrategy(new SomeCustomBackoffStrategy())
            .build();

Custom backoff strategies can be created by implementing the **com.evanlennick.retry4j.backoff.BackoffStrategy** interface.

### Simple Configs

Retry4j offers some predefined configurations if you just want to get rolling and worry about tweaking later. These configs can be utilized like so:

    new RetryConfigBuilder()
        .fixedBackoff5Tries10Sec()
        .build();

    new RetryConfigBuilder()
        .exponentialBackoff5Tries5Sec()
        .build();

    new RetryConfigBuilder()
        .fiboBackoff7Tries5Sec()
        .build();

    new RetryConfigBuilder()
        .randomExpBackoff10Tries60Sec()
        .build();

### CallExecutor

Executing your code with retry logic is as simple as instantiating a **CallExecutor** with your configuration and then calling execute:

    new CallExecutor(config).execute(callable);
    
The CallExecutor expects that your logic is wrapped in a **java.util.concurrent.Callable**.

### CallResults

After the executor successfully completes or throws a RetriesExhaustedException, a **CallResults** object will returned or included in the exception. This object will contain detailed information about the call execution including the number of total tries, the total elapsed time and whether or not the execution was considered successful upon completion.

    CallResults results = new CallExecutor(config).execute(callable);
    System.out.println(results.getResult()); //this will be populated if your callable returns a value
    System.out.println(results.wasSuccessful());
    System.out.println(results.getCallName());
    System.out.println(results.getTotalDurationElapsed());
    System.out.println(results.getTotalTries());
    System.out.println(results.getLastExceptionThatCausedRetry());
    
or

    try {  
        new CallExecutor(config).execute(callable);
    } catch(RetriesExhaustedException cfe) {
        CallResults results = cfe.getCallResults();
        System.out.println(results.wasSuccessful());
        System.out.println(results.getCallName());
        System.out.println(results.getTotalDurationElapsed());
        System.out.println(results.getTotalTries());
        System.out.println(results.getLastExceptionThatCausedRetry());
    }

### Retry4jException

Retry4j has the potential throw several unique exceptions when building a config, when executing retries or upon completing execution (if unsuccessful). All Retry4j exceptions are unchecked. You do not have to explicitly catch them if you wish to let them bubble up cleanly to some other exception handling mechanism. The types of **Retry4jException**'s are:

* **UnexpectedException** - Occurs when an exception is thrown from the callable code. Only happens if the exception thrown was not one specified by the *retryOnSpecificExceptions()* method in the config or if the *retryOnAnyException()* option was not specified as part of the config.
* **RetriesExhaustedException** - This indicates the callable code was retried the maximum number of times specified in the config via *withMaxNumberOfTries()* and failed all tries.
* **InvalidRetryConfigException** - This exception is thrown when the RetryConfigBuilder detects that the invoker attempted to build an invalid config object. This will come with a specific error message indicating the problem. Common issues might be trying to specify more than one backoff strategy (or specifying none), specifying more than one exceptions strategy or forgetting to specify something mandatory such as the maximum number of tries.

***NOTE:*** Validation on the **RetryConfigBuilder** can be disabled to prevent **InvalidRetryConfigException**'s from ever being thrown. This is not recommended in application code but may be useful when writing test code. Examples of how to disable it:

    new RetryConfigBuilder().setValidationEnabled(false).build()
    
or

    new RetryConfigBuilder(false);

### Listeners

RetryListener's are offered in case you want to be able to add logic that will execute immediately after a failed try or immediately before the next retry (for example, you may want to log or output a statement when something is retrying). These listeners can be specified like so:

    CallExecutor executor = new CallExecutor(config);
    
    executor.registerRetryListener((AfterFailedTryListener) results -> {
        //whatever logic you want to execute after a failed try
    });
    
    executor.registerRetryListener((BeforeNextTryListener) results -> {
        //whatever logic you want to execute before the next try
    });

Two additional listeners are also offered to indicate when a series of retries has succeed or failed. They can be specified like so:

    executor.registerRetryListener((OnSuccessListener) results -> {
        //whatever logic you want to execute after retry execution has completed successfully
    });
    
    executor.registerRetryListener((OnFailureListener) results -> {
        //whatever logic you want to execute after retry execution has exhausted all retries
    });

***NOTE:*** If you register an ```OnFailureListener``` with the CallExecutor, it will toggle off the throwing of **RetriesExhaustedException**'s. Handling a failure after retries are exhausted will be left up to the listener.

If you wish to execute any sort of cleanup or finalization logic that will execute no matter what the final results is (success, exhausted retries, unexpected exception throw) you can implement the following listener:

    executor.registerRetryListener((OnCompletionListener) results -> {
        //whatever logic you want to execute after call execution has completed
    });

### Async Support

Retry4j has some built in support for executing and retrying on one or more threads in an asynchronous fashion. The 
`AsyncCallExecutor` utilizes threading and async mechanisms via Java's `ExecutorService` and `CompletableFuture` 
API's. A basic example of this in action with a single call:

    AsyncCallExecutor<Boolean> executor = new AsyncCallExecutor<>(config);
    CompletableFuture<CallResults<Boolean>> future = executor.execute(callable);
    CallResults<Boolean> results = future.get();
    
In the above case, the logic in the callable will begin executing immediately upon `executor.execute(callable)` being 
called. However, the callable (with retries) will execute on another thread and the original thread that started 
execution will not be blocked until `future.get()` is called (if it hasn't completed).
    
This executor can also be used to trigger several Callable's in parallel:

    AsyncCallExecutor<Boolean> executor = new AsyncCallExecutor<>(retryOnAnyExceptionConfig);

    CompletableFuture<CallResults<Boolean>> future1 = executor.execute(callable1);
    CompletableFuture<CallResults<Boolean>> future2 = executor.execute(callable2);
    CompletableFuture<CallResults<Boolean>> future3 = executor.execute(callable3);

    CompletableFuture combinedFuture = CompletableFuture.allOf(future1, future2, future3);
    combinedFuture.get();
    
In both of these examples, the `AsyncCallExecutor` takes care of instantiating and using a simple, default 
`ExecutorService` for managing threads. If you wish to define what `ExecutorService` gets used by the 
`AsyncCallExecutor`, you can pass it in as part of the constructor like this:

    ExecutorService executorService = Executors.newFixedThreadPool(10);
    new AsyncCallExecutor<>(config, executorService);
    
You can register retry listeners and configuration on an `AsyncCallExecutor` in the same fashion as the normal, 
synchronous `CallExecutor`. All calls in all threads that are triggered from an `AsyncCallExecutor` after its 
construction will use the same listeners and configuration.

All of this async and threading functionality is new as of `0.9.0` and may need some time to settle before it is 
completely stable and mature.

## Other Notes

Retry4j follows semantic versioning: http://semver.org/. As it is still version 0.x.x and prior to 1.0.0, the API is subject to rapid change and breakage.

There are a number of other retry libraries for Java and the JVM that might better suit your needs. Please feel free to check out the following libraries as well if Retry4j doesn't fit:

* Guava Retrying - https://github.com/rholder/guava-retrying
* Failsafe - https://github.com/jhalterman/failsafe
* Spring Retry - https://github.com/spring-projects/spring-retry
