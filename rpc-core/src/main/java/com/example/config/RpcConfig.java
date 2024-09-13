package com.example.config;

import com.example.serializer.SerializerKeys;
import lombok.Data;

/**
 * RPC框架配置
 */
@Data
public class RpcConfig {
    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.JDK;
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
    private String host = "localhost";

    /**
     * 服务器端口号
     */
    private Integer serverPost = 8080;
}
