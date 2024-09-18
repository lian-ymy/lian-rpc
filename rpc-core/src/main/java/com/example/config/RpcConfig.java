package com.example.config;

import com.example.loadbalancer.LoadBalancer;
import com.example.loadbalancer.LoadBalancerKeys;
import com.example.serializer.SerializerKeys;
import lombok.Data;

/**
 * RPC框架配置
 */
@Data
public class RpcConfig {
    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();
    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.JDK;
    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;
    /**
     * 模拟调用
     */
    private boolean mock = false;
    /**
     * 名称
     */
    private String name = "lian-rpc";

    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;
}
