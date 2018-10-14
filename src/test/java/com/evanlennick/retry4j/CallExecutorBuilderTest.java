package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.evanlennick.retry4j.listener.RetryListener;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class CallExecutorBuilderTest {

    private RetryListener afterFailedTryListener = status -> {};
    private RetryListener beforeNextTryListener = status -> {};
    private RetryListener onSuccessListener = status -> {};
    private RetryListener onFailureListener = status -> {};
    private RetryListener onCompletionListener = status -> {};
    private RetryConfig retryConfig = new RetryConfigBuilder().fixedBackoff5Tries10Sec().build();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Test
    public void shouldBuildCallExecutorWithConfigAndListeners() {
        CallExecutor callExecutor = new CallExecutorBuilder()
                .config(retryConfig)
                .afterFailedTryListener(afterFailedTryListener)
                .beforeNextTryListener(beforeNextTryListener)
                .onSuccessListener(onSuccessListener)
                .onFailureListener(onFailureListener)
                .onCompletionListener(onCompletionListener)
                .build();

        assertThat(callExecutor.getConfig()).isEqualTo(retryConfig);
        assertThat(callExecutor.getAfterFailedTryListener()).isEqualTo(afterFailedTryListener);
        assertThat(callExecutor.getBeforeNextTryListener()).isEqualTo(beforeNextTryListener);
        assertThat(callExecutor.getOnSuccessListener()).isEqualTo(onSuccessListener);
        assertThat(callExecutor.getOnFailureListener()).isEqualTo(onFailureListener);
        assertThat(callExecutor.getOnCompletionListener()).isEqualTo(onCompletionListener);
    }

    @Test
    public void shouldBuildAsyncCallExecutorWithConfigAndListeners() {
        AsyncCallExecutor asyncCallExecutor = new CallExecutorBuilder()
                .config(retryConfig)
                .afterFailedTryListener(afterFailedTryListener)
                .beforeNextTryListener(beforeNextTryListener)
                .onSuccessListener(onSuccessListener)
                .onFailureListener(onFailureListener)
                .onCompletionListener(onCompletionListener)
                .buildAsync();

        assertThat(asyncCallExecutor.getConfig()).isEqualTo(retryConfig);
        assertThat(asyncCallExecutor.getAfterFailedTryListener()).isEqualTo(afterFailedTryListener);
        assertThat(asyncCallExecutor.getBeforeNextTryListener()).isEqualTo(beforeNextTryListener);
        assertThat(asyncCallExecutor.getOnSuccessListener()).isEqualTo(onSuccessListener);
        assertThat(asyncCallExecutor.getOnFailureListener()).isEqualTo(onFailureListener);
        assertThat(asyncCallExecutor.getOnCompletionListener()).isEqualTo(onCompletionListener);
    }

    @Test
    public void shouldBuildAsyncCallExecutorWithConfig_And_Listeners_And_ExecutorService() {
        AsyncCallExecutor asyncCallExecutor = new CallExecutorBuilder()
                .config(retryConfig)
                .afterFailedTryListener(afterFailedTryListener)
                .beforeNextTryListener(beforeNextTryListener)
                .onSuccessListener(onSuccessListener)
                .onFailureListener(onFailureListener)
                .onCompletionListener(onCompletionListener)
                .buildAsync(executorService);

        assertThat(asyncCallExecutor.getConfig()).isEqualTo(retryConfig);
        assertThat(asyncCallExecutor.getAfterFailedTryListener()).isEqualTo(afterFailedTryListener);
        assertThat(asyncCallExecutor.getBeforeNextTryListener()).isEqualTo(beforeNextTryListener);
        assertThat(asyncCallExecutor.getOnSuccessListener()).isEqualTo(onSuccessListener);
        assertThat(asyncCallExecutor.getOnFailureListener()).isEqualTo(onFailureListener);
        assertThat(asyncCallExecutor.getOnCompletionListener()).isEqualTo(onCompletionListener);
        assertThat(asyncCallExecutor.getExecutorService()).isEqualTo(executorService);
    }

}
