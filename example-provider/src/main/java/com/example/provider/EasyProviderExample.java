package com.example.provider;

import com.example.common.service.UserService;
import com.example.registry.LocalRegistry;
import com.example.server.HttpServer;
import com.example.server.VertxHttpServer;

/**
 * 服务提供者实例类
 */
public class EasyProviderExample {
    public static void main(String[] args) {
        //将服务信息注册到注册中心中
        LocalRegistry.registry(UserService.class.getName(),UserServiceImpl.class);

        //提供服务
        HttpServer server = new VertxHttpServer();
        server.doStart(8080);
    }
}
