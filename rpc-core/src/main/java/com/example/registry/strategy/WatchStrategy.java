package com.example.registry.strategy;

/**
 * 注册中心监听策略
 */
public interface WatchStrategy {

    /**
     * 基于服务节点信息完成监听；
     *
     * @param serviceNodeKey 节点格式 /ROOT_PATH/serviceName:version/ip:port
     */
    void watch(String serviceNodeKey);
}
