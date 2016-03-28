package com.evanlennick.retry4j;

import org.testng.annotations.Test;

public class RetryExecutorTest_AnnotationConfigTest {

    @Test(enabled = false) //just stubbing this out for now
    public void verifyAnnotationConfigGetsIngested() {
        CallExecutor<String> executor = new CallExecutor<>();
        executor.executeUsingAnnotationConfig(null);
    }

}
