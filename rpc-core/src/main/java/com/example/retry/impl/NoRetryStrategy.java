package com.example.retry.impl;

import com.example.model.RpcResponse;
import com.example.retry.RetryStrategy;

import java.util.concurrent.Callable;

/**
 * 不重试，重试策略
 */
public class NoRetryStrategy implements RetryStrategy {
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
