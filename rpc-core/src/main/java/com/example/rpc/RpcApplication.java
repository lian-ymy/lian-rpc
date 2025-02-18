package com.example.rpc;

import com.example.config.RegistryConfig;
import com.example.config.RpcConfig;
import com.example.constant.RpcConstant;
import com.example.registry.Registry;
import com.example.registry.RegistryFactory;
import com.example.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC框架应用
 * 相当于holder，存放了项目全局用到的变量，双检锁单例实现
 */
@Slf4j
public class RpcApplication {
    private static volatile RpcConfig rpcConfig;

    //是否需要启动 RPC 框架服务器
    public static volatile boolean needServer;

    public static void setRpcConfig(RpcConfig newRpcConfig) {
        if (rpcConfig == null) {
            synchronized (RpcConfig.class) {
                if (rpcConfig == null) {
                    rpcConfig = newRpcConfig;
                }
            }
        }
    }

    //启动服务注册中心 - 心跳检测
    public static void initRegistry(RpcConfig newConfig) {
        if (rpcConfig == null) {
            rpcConfig = newConfig;
            log.info("Rpc Config init succeed!, config = {}", newConfig);
        }

        //通过 RpcConfig 获取 RegistryConfig
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();

        //通过 RegistryConfig 获取到 RegistryType 实例化 Registry
        Registry registry = RegistryFactory.getRegistry(registryConfig.getRegistryType());

        //调用 Registry 的初始化加载RegistryConfig
        registry.init(registryConfig);

        //创建并注册 Shutdown Hook, JVM 退出时执行操作
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destory));
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
        initRegistry(newRpcConfig);
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
