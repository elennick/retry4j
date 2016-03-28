package com.evanlennick.retry4j;

import com.evanlennick.retry4j.config.RetryConfig;

import java.util.concurrent.Callable;

public final class AnnotationUtil {

    private AnnotationUtil() {}

    public static RetryConfig getConfig(Callable<?> callable) {
        return null;
    }

}
