package com.example.rpc;

import com.example.config.RpcConfig;
import com.example.constant.RpcConstant;
import com.example.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC框架应用
 * 相当于holder，存放了项目全局用到的变量，双检锁单例实现
 */
public class RpcApplication {
    private static volatile RpcConfig rpcConfig;

    /**
     * 框架初始化，可以传入自定义配置
     * @param newRpcConfig
     */
    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
    }

    /**
     * 初始化
     */
    public static void init() {
        RpcConfig newRpcConfig;
        try {
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    /**
     * 获取配置
     * @return
     */
    public static RpcConfig getRpcConfig() {
        if(rpcConfig == null) {
            //锁住整个类，保证同一个时间只有一个线程能够获得当前这把锁
            synchronized (RpcConfig.class) {
                if(rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
