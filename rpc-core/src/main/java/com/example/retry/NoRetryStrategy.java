package com.example.retry;

import com.example.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 不重试，重试策略
 */
public class NoRetryStrategy implements RetryStrategy{
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
