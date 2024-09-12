package com.example.config;

import jdk.jfr.DataAmount;
import lombok.Data;

/**
 * RPC框架配置
 */
@Data
public class RpcConfig {
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
