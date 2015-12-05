![Build Status](https://travis-ci.org/elennick/retry4j.svg?branch=master&link=https://travis-ci.org/elennick/retry4j&link=https://travis-ci.org/elennick/retry4j) ![License](https://img.shields.io/packagist/l/doctrine/orm.svg) ![Maven Central](https://img.shields.io/maven-central/v/com.evanlennick/retry4j.svg)

## Retry4j

Retry4j is a simple Java library to assist with retrying transient failure situations or unreliable code. Code that relies on connecting to an external resource that is intermittently available (ie: a REST API or external database connection) is a good example of where this type of logic is useful.

There are several libraries that have similar capabilities this but I found them to either not work as advertised, to be overly complex or to be poorly documented. Retry4j aims to be readable, well documented and streamlined.

## Basic Code Examples

### Synchronous

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
      //the call threw an unexpected exception that was not specified to retry on
    }

Or more simple using one of the predefined config options and not checking exceptions:

    Callable<Object> callable = () -> {
        //code that you want to retry
    };

    RetryConfig config = new RetryConfigBuilder()
        .exponentialBackoff5Tries5Sec()
        .build();

    CallResults<Object> results = new CallExecutor(config).execute(callable);

### Asynchronous

    Callable<Object> callable = () -> {
        //code that you want to retry
    };

    RetryConfig config = new RetryConfigBuilder()
        .exponentialBackoff5Tries5Sec()
        .build();

    CallExecutor executor = new CallExecutor(config);
    
    executor.registerRetryListener((OnFailureListener) results -> { /** some code to execute on failure **/ });
    executor.registerRetryListener((OnSuccessListener) results -> { /** some code to execute on success **/ });
    executor.executeAsync(callable);

## Dependencies

### Maven

    <dependency>
        <groupId>com.evanlennick</groupId>
        <artifactId>retry4j</artifactId>
        <version>0.6.0</version>
    </dependency>

### SBT

    libraryDependencies += "com.evanlennick" % "retry4j" % "0.6.0"

### Gradle

    compile "com.evanlennick:retry4j:0.6.0"

## Documentation

### General

Retry4j does not require any external dependencies. It does require that you are using Java 8 or newer. Javadocs are hosted at http://www.javadoc.io/doc/com.evanlennick/retry4j/0.6.0.

### Exception Handling Config

If you do not specify how exceptions should be handled or explicitly say **failOnAnyException()**, the CallExecutor will fail and throw an **UnexpectedException** when encountering exceptions while running. Use this configuration if you want the executor to cease its work when it runs into any exception at all.

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

To specify the maximum number of tries that should be attempted, specify an integer value in the config using the **withMaxNumberOfTries()** method. The executor will attempt to execute the call the number of times specified and if it does not succeed after all tries have been exhausted, it will throw a **RetriesExhaustedException**.

    RetryConfig config = new RetryConfigBuilder()
            .withMaxNumberOfTries(5)
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

### Async Support

Retry4j has asynchronous support of calls by using the ```executeAsync()``` method of the CallExecutor. Right now this support is very beta and has known issues, especially when attempting to execute more than one call asynchonously at the same time. These issues should be cleaned up in the 0.7.0 release and the API related to asynchronous calls will likely change.

        CallExecutor executor = new CallExecutor(config);
        executor.executeAsync(callable);

Using the ```executeAsync()``` method will execute the passed call in an asynchronous, nonblocking fashion.

***NOTE:*** If no ```java.util.concurrent.ExecutorService``` is specified, the Retry4j CallExecutor will default to using a fixed thread pool with 10 threads. If you want to specify an ExecutorService that is initialized with your own configuration, you can do so by calling ```CallExecutor.setExecutorService(executorService)```. For example:

        ExecutorService customExecutorService = Executors.newScheduledThreadPool(10);
        
        CallExecutor callExecutor = new CallExecutor(config);
        callExecutor.setExecutorService(customExecutorService);
        
        callExecutor.executeAsync(callable);
