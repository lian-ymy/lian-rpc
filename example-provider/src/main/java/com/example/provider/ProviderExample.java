package com.example.provider;

import com.example.common.service.UserService;
import com.example.registry.LocalRegistry;
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
        LocalRegistry.registry(UserService.class.getName(),UserServiceImpl.class);

        //启动web服务
        VertxHttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPost());
    }
}
