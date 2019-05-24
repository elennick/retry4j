![Build Status](https://travis-ci.org/elennick/retry4j.svg?branch=master&link=https://travis-ci.org/elennick/retry4j&link=https://travis-ci.org/elennick/retry4j) [![Coverage Status](https://coveralls.io/repos/github/elennick/retry4j/badge.svg?branch=master)](https://coveralls.io/github/elennick/retry4j?branch=master) ![Maven Central](https://img.shields.io/maven-central/v/com.evanlennick/retry4j.svg) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Retry4j

Retry4j is a simple Java library to assist with retrying transient failure situations or unreliable code. Retry4j aims to be readable, well documented and streamlined.

## Table of Contents

* [Basic Code Examples](#basic-code-examples)
    * [Handling Failures with Exceptions](#handling-failures-with-exceptions)
    * [Handling All Results With Listeners](#handling-all-results-with-listeners)
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
    * [Custom Backoff Strategies](#custom-backoff-strategies)
    * [Simple Configs](#simple-configs)
    * [CallExecutor](#callexecutor)
    * [Call Status](#call-status)
    * [Retry4jException](#retry4jexception)
    * [Listeners](#listeners)
    * [Async Support](#async-support)
    * [Logging](#logging)
* [Other Notes](#other-notes)
      
## Basic Code Examples

### Handling Failures with Exceptions

```java
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
    Status<Object> status = new CallExecutorBuilder()
        .config(config)
        .build()
        .execute(callable);
    Object object = status.getResult(); //the result of the callable logic, if it returns one
} catch(RetriesExhaustedException ree) {
    //the call exhausted all tries without succeeding
} catch(UnexpectedException ue) {
    //the call threw an unexpected exception
}
```

Or more simple using one of the predefined config options and not checking exceptions:

```java
Callable<Object> callable = () -> {
    //code that you want to retry
};

RetryConfig config = new RetryConfigBuilder()
    .exponentialBackoff5Tries5Sec()
    .build();

Status<Object> status = new CallExecutorBuilder().config(config).build().execute(callable);
```

### Handling All Results with Listeners

```java
Callable<Object> callable = () -> {
    //code that you want to retry
};

RetryConfig config = new RetryConfigBuilder()
        .exponentialBackoff5Tries5Sec()
        .build();

CallExecutor executor = new CallExecutorBuilder<>()
        .config(config).
        .onSuccess(s -> { //do something on success })
        .onFailure(s -> { //do something after all retries are exhausted })
        .afterFailedTry(s -> { //do something after a failed try })
        .beforeNextTry(s -> { //do something before the next try })
        .onCompletion(s -> { //do some cleanup })
        .build();
        
executor.execute(callable);
```

## Dependencies

### Maven

```xml
<dependency>
    <groupId>com.evanlennick</groupId>
    <artifactId>retry4j</artifactId>
    <version>0.15.0</version>
</dependency>
```

### SBT

```sbt
libraryDependencies += "com.evanlennick" % "retry4j" % "0.15.0"
```
### Gradle

```groovy
compile "com.evanlennick:retry4j:0.15.0"
```

## Usage

### General

Retry4j does not require any external dependencies. It does require that you are using Java 8 or newer.

* Javadocs are hosted at: http://www.javadoc.io/doc/com.evanlennick/retry4j/0.15.0.
* Continuous integration results are available via Travis CI here: https://travis-ci.org/elennick/retry4j.
* Code coverage information is available via Coveralls here: https://coveralls.io/github/elennick/retry4j.
* Examples of Retry4j in use are documented here: https://github.com/elennick/retry4j-examples

### Exception Handling Config

If you do not specify how exceptions should be handled or explicitly say **failOnAnyException()**, the CallExecutor will fail and throw an **UnexpectedException** when encountering exceptions while running. Use this configuration if you want the executor to cease its work when it runs into any exception at all.

```java
RetryConfig config = new RetryConfigBuilder()
        .failOnAnyException()
        .build();
```

If you want to specify specific exceptions that should cause the executor to continue and retry on encountering, do so using the **retryOnSpecificExceptions()** config method. This method can accept any number of exceptions if there is more than one that should indicate the executor should continue retrying. All other unspecified exceptions will immediately interupt the executor and throw an **UnexpectedException**.

```java
RetryConfig config = new RetryConfigBuilder()
        .retryOnSpecificExceptions(ConnectException.class, TimeoutException.class)
        .build();
```

If you want the executor to continue to retry on all encountered exceptions, specify this using the **retryOnAnyException()** config option.

```java
RetryConfig config = new RetryConfigBuilder()
        .retryOnAnyException()
        .build();
```

If you want the executor to continue to retry when encountered exception's cause is among a list of exceptions, then specify **retryOnCausedBy()** config option
```java
RetryConfig config = new RetryConfigBuilder()
        .retryOnCausedBy()
        .retryOnSpecificExceptions(ConnectException.class, TimeoutException.class)
        .build();
```

If you want the executor to continue to retry on all encountered exceptions EXCEPT for a few specific ones, specify this using the **retryOnAnyExceptionExcluding()** config option. If this exception strategy is chosen, only the exceptions specified or their subclasses will interupt the executor and throw an **UnexpectedException**.

```java
RetryConfig config = new RetryConfigBuilder()
        .retryOnAnyExceptionExcluding(CriticalFailure.class, DontRetryOnThis.class)
        .build();
```

NOTE: When using `retryOnSpecificExceptions` and `retryOnAnyExceptionExcluding`, the call executor will also take into account if the encountered exceptions are subclasses of the types you specified. For example, if you tell the configuration to retry on any `IOException`, the executor will retry on a `FileNotFoundException` which is a subclass of `IOException`.

If you do not want to use these built-in mechanisms for retrying on exceptions, you can override them and create custom logic:

```java
RetryConfig config = new RetryConfigBuilder()
        .retryOnCustomExceptionLogic(ex -> {
            //return true to retry, otherwise return false
        })
        .build();
```

If you create custom exception logic, no other built-in retry-on-exception configuration can be used at the same time.

### Value Handling Config

If you want the executor to retry based on the returned value from the Callable:

```java
RetryConfig config = retryConfigBuilder
        .retryOnReturnValue("retry on this value!")
        .build();
```

This can be used in combination with exception handling configuration like so:

```java
RetryConfig config = retryConfigBuilder
        .retryOnSpecificExceptions(FileNotFoundException.class)
        .retryOnReturnValue("retry on this value!")
        .build();
```
            
In the above scenario, the call execution will be considered a failure and a retry will be triggered if 
`FileNotFoundException.class` is thrown OR if the String `retry on this value!` is returned from the Callable logic.

To retry only using return values, you can disable exception retries using the `failOnAnyException()` configuration option:

```java
RetryConfig config = retryConfigBuilder
        .failOnAnyException()
        .retryOnReturnValue("retry on this value!")
        .build();
```

### Timing Config

To specify the maximum number of tries that should be attempted, specify an integer value in the config using the **withMaxNumberOfTries()** method. The executor will attempt to execute the call the number of times specified and if it does not succeed after all tries have been exhausted, it will throw a **RetriesExhaustedException**.


```java
RetryConfig config = new RetryConfigBuilder()
        .withMaxNumberOfTries(5)
        .build();
``` 

If you do not wish to have a maximum and want retries to continue indefinitely, instead us the `retryIndefinitely()` 
option:

```java
RetryConfig config = new RetryConfigBuilder()
        .retryIndefinitely()
        .build();
````

To specify the delay in between each try, use the **withDelayBetweenTries()** config method. This method will accept a Java 8 Duration object or an integer combined with a ChronoUnit.

```java
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
```

### Backoff Strategy Config

Retry4j has built in support for several backoff strategies. They can be specified like so:

```java
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
```

### Custom Backoff Strategies

Custom backoff strategies can be specified like so:

```java
RetryConfig config = new RetryConfigBuilder()
        .withBackoffStrategy(new SomeCustomBackoffStrategy())
        .build();
```

...where `SomeCustomBackoffStrategy` is an object that implements the `com.evanlennick.retry4j.backoff.BackoffStrategy` interface. The only mandatory method to implement is `getDurationToWait()` which determines how long to wait between each try. Optionally, the `validateConfig()` method can also be implemented if your backoff strategy needs to verify that the configuration being used is valid.

For examples creating backoff strategies, check out the provided implementations [here](https://github.com/elennick/retry4j/tree/master/src/main/java/com/evanlennick/retry4j/backoff).

### Simple Configs

Retry4j offers some predefined configurations if you just want to get rolling and worry about tweaking later. These configs can be utilized like so:

```java
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
```

### CallExecutor

Executing your code with retry logic is as simple as building a **CallExecutor** using **CallExecutorBuilder** with your configuration and then calling execute:

```java
new CallExecutorBuilder.config(config).build().execute(callable);
``` 

The CallExecutor expects that your logic is wrapped in a **java.util.concurrent.Callable**.

### Call Status

After the executor successfully completes or throws a RetriesExhaustedException, a **Status** object will returned or included in the exception. This object will contain detailed information about the call execution including the number of total tries, the total elapsed time and whether or not the execution was considered successful upon completion.

```java
Status status = new CallExecutorBuilder().config(config).build().execute(callable);
System.out.println(status.getResult()); //this will be populated if your callable returns a value
System.out.println(status.wasSuccessful());
System.out.println(status.getCallName());
System.out.println(status.getTotalDurationElapsed());
System.out.println(status.getTotalTries());
System.out.println(status.getLastExceptionThatCausedRetry());
```  

or

```java
    try {  
        new CallExecutorBuilder().config(config).build().execute(callable);
    } catch(RetriesExhaustedException cfe) {
        Status status = cfe.getStatus();
        System.out.println(status.wasSuccessful());
        System.out.println(status.getCallName());
        System.out.println(status.getTotalDurationElapsed());
        System.out.println(status.getTotalTries());
        System.out.println(status.getLastExceptionThatCausedRetry());
    }
```

### Retry4jException

Retry4j has the potential throw several unique exceptions when building a config, when executing retries or upon completing execution (if unsuccessful). All Retry4j exceptions are unchecked. You do not have to explicitly catch them if you wish to let them bubble up cleanly to some other exception handling mechanism. The types of **Retry4jException**'s are:

* **UnexpectedException** - Occurs when an exception is thrown from the callable code. Only happens if the exception thrown was not one specified by the *retryOnSpecificExceptions()* method in the config or if the *retryOnAnyException()* option was not specified as part of the config.
* **RetriesExhaustedException** - This indicates the callable code was retried the maximum number of times specified in the config via *withMaxNumberOfTries()* and failed all tries.
* **InvalidRetryConfigException** - This exception is thrown when the RetryConfigBuilder detects that the invoker attempted to build an invalid config object. This will come with a specific error message indicating the problem. Common issues might be trying to specify more than one backoff strategy (or specifying none), specifying more than one exceptions strategy or forgetting to specify something mandatory such as the maximum number of tries.

***NOTE:*** Validation on the **RetryConfigBuilder** can be disabled to prevent **InvalidRetryConfigException**'s from ever being thrown. This is not recommended in application code but may be useful when writing test code. Examples of how to disable it:

```java
new RetryConfigBuilder().setValidationEnabled(false).build()
```

or

```java
new RetryConfigBuilder(false);
```

### Listeners

Listeners are offered in case you want to be able to add logic that will execute immediately after a failed try or immediately before the next retry (for example, you may want to log or output a statement when something is retrying). These listeners can be specified like so:

```java
CallExecutor executor = new CallExecutorBuilder().config(config).build();

executor.afterFailedTry(s -> { 
    //whatever logic you want to execute immediately after each failed try
});

executor.beforeNextTry(s -> {
    //whatever logic you want to execute immediately before each try
});
```

Two additional listeners are also offered to indicate when a series of retries has succeeded or failed. They can be specified like so:

```java
executor.onSuccess(s -> {
    //whatever logic you want to execute when the callable finishes successfully
});

executor.onFailure(s -> {
    //whatever logic you want to execute when all retries are exhausted
});
```

***NOTE:*** If you register a failure listener with the CallExecutor, it will toggle off the throwing of 
**RetriesExhaustedException**'s. Handling a failure after retries are exhausted will be left up to the listener.

If you wish to execute any sort of cleanup or finalization logic that will execute no matter what the final results is (success, exhausted retries, unexpected exception throw) you can implement the following listener:


```java
executor.onCompletion(s -> {
    //whatever logic you want to execute after the executor has completed, regardless of status
});
```

Listeners can be chained together:

```java
new CallExecutorBuilder<>()
       .config(config)
       .onSuccess(s -> System.out.println("Success!"))
       .onCompletion(s -> System.out.println("Retry execution complete!"))
       .onFailure(s -> System.out.println("Failed! All retries exhausted..."))
       .afterFailedTry(s -> System.out.println("Try failed! Will try again in 0ms."))
       .beforeNextTry(s -> System.out.println("Trying again..."))
       .build()
       .execute(callable);
```

### Async Support

Retry4j has some built in support for executing and retrying on one or more threads in an asynchronous fashion. The 
`AsyncCallExecutor` utilizes threading and async mechanisms via Java's `CompletableFuture` API. A basic example of this 
in action with a single call:

```java
AsyncCallExecutor<Boolean> executor = new CallExecutorBuilder().config(config).buildAsync();
CompletableFuture<Status<Boolean>> future = executor.execute(callable);
Status<Boolean> status = future.get();
```

In the above case, the logic in the callable will begin executing immediately upon `executor.execute(callable)` being 
called. However, the callable (with retries) will execute on another thread and the original thread that started 
execution will not be blocked until `future.get()` is called (if it hasn't completed).
    
This executor can also be used to trigger several Callable's in parallel:

```java
AsyncCallExecutor<Boolean> executor = new CallExecutorBuilder().config(retryOnAnyExceptionConfig).buildAsync();

CompletableFuture<Status<Boolean>> future1 = executor.execute(callable1);
CompletableFuture<Status<Boolean>> future2 = executor.execute(callable2);
CompletableFuture<Status<Boolean>> future3 = executor.execute(callable3);

CompletableFuture.allOf(future1, future2, future3).join();
```

If you wish to define a thread pool to be used by your `AsyncCallExecutor`, you can define and pass in an 
`ExecutorService` in the constructor. When using this pattern, it's important to remember that this thread pool will not
shut itself down and you will have to explicitly call `shutdown()` on the `ExecutorService` if you want it to be cleaned
up.

```java
ExecutorService executorService = Executors.newFixedThreadPool(10);
new CallExecutorBuilder().config(config).buildAsync(executorService);
```

You can register retry listeners and configuration on an `AsyncCallExecutor` in the same fashion as the normal, 
synchronous `CallExecutor`. All calls in all threads that are triggered from an `AsyncCallExecutor` after its 
construction will use the same listeners and configuration.

### Logging

Retry4j contains detailed internal logging using [SLF4J](https://www.slf4j.org/manual.html). If you do not specify a SLF4J implementation, these logs will be discarded. If you do provide an implementation (eg: Logback, Log4J, etc) you can specify the log level on the `com.evanlennick.retry4j` package to set Retry4j logging to a specific level.

## Other Notes

Retry4j follows semantic versioning: http://semver.org/. As it is still version 0.x.x and prior to 1.0.0, the API is subject to rapid change and breakage.

There are a number of other retry libraries for Java and the JVM that might better suit your needs. Please feel free to check out the following libraries as well if Retry4j doesn't fit:

* Guava Retrying - https://github.com/rhuffman/re-retrying
* Failsafe - https://github.com/jhalterman/failsafe
* Spring Retry - https://github.com/spring-projects/spring-retry
* resilience4j - https://github.com/resilience4j/resilience4j
