package com.example.consumer;

import com.example.bootstrap.ConsumerBootstrap;
import com.example.common.model.User;
import com.example.common.service.UserService;
import com.example.proxy.ServiceProxyFactory;

public class ConsumerExample {
    public static void main(String[] args) {
        //服务提供者初始化
        ConsumerBootstrap.init();

        //获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("镜流");
        User newUser = userService.getUser(user);
        if (newUser == null) {
            System.out.println("当前获取的用户为空!");
        } else {
            System.out.println("获取的新用户名称为：" + newUser.getName());
        }
    }
}
