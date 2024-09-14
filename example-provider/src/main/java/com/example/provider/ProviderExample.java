package com.example.provider;

import com.example.common.service.UserService;
import com.example.config.RegistryConfig;
import com.example.config.RpcConfig;
import com.example.constant.RpcConstant;
import com.example.model.ServiceMetaInfo;
import com.example.registry.LocalRegistry;
import com.example.registry.Registry;
import com.example.registry.RegistryFactory;
import com.example.rpc.RpcApplication;
import com.example.server.VertxHttpServer;

/**
 * 服务提供者
 */
public class ProviderExample {
    public static void main(String[] args) {
        //RPC框架初始化
        RpcApplication.init();

        //注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.registry(serviceName, UserServiceImpl.class);

        //注册服务到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //启动web服务
        VertxHttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(rpcConfig.getServerPort());
    }
}
