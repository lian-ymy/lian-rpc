package com.example.retry.impl;

import com.example.model.RpcResponse;
import com.example.retry.RetryStrategy;
import com.github.rholder.retry.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 递增时间序列重试等待策略
 */
@Slf4j
public class IncreasingTimeRetryStrategy implements RetryStrategy {

    /**
     * 重试策略
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class)
                .withWaitStrategy(WaitStrategies.incrementingWait(2, TimeUnit.SECONDS, 2, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterDelay(10000))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("重试次数{}，重试时间{}", attempt.getAttemptNumber(), attempt.getDelaySinceFirstAttempt());
                    }
                })
                .build();
        return retryer.call(callable);
    }
}
