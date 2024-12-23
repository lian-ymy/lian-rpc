package com.example.loadbalancer;

/**
 * 负载均衡器键名常量
 */
public interface LoadBalancerKeys {
    /**
     * 轮询
     */
    String ROUND_ROBIN = "roundRobin";

    /**
     * 随机
     */
    String RANDOM = "random";

    /**
     * 哈希
     */
    String CONSISTENT_HASH = "consistentHash";
}
