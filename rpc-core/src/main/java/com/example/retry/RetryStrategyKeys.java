package com.example.retry;

/**
 * 重试策略键名常量
 */
public interface RetryStrategyKeys {
    /**
     * 不重试
     */
    String NO = "no";

    /**
     * 固定时间间隔
     */
    String FIXED_INTERVAL = "fixedInterval";

    /**
     * 递增时间间隔
     */
    String INCREASING_TIME = "increasingTime";

    /**
     * 指数时间间隔
     */
    String EXPONENTIAL = "exponential";
}
