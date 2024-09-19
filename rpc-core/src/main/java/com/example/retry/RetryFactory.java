package com.example.retry;

import com.example.spi.SpiLoader;

/**
 * 重试策略工厂类
 */
public class RetryFactory {
    static {
        SpiLoader.load(RetryStrategy.class);
    }

    /**
     * 默认实现的重试策略
     */
    private static RetryStrategy DEFAULT_RETRY_STRATEGY = new NoRetryStrategy();

    /**
     * 获取指定的重试策略实现类
     * @param retryStrategy
     * @return
     */
    public static RetryStrategy getInstance(String retryStrategy) {
        return SpiLoader.getInstance(RetryStrategy.class, retryStrategy);
    }

}
