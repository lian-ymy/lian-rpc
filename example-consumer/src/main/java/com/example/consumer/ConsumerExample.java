package com.example.consumer;

import com.example.common.model.User;
import com.example.common.service.UserService;
import com.example.config.RpcConfig;
import com.example.proxy.ServiceProxy;
import com.example.proxy.ServiceProxyFactory;
import com.example.utils.ConfigUtils;

public class ConsumerExample {
    public static void main(String[] args) {
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("镜流");
        User newUser = userService.getUser(user);
        if (newUser == null) {
            System.out.println("newUser == null");
        } else {
            System.out.println("获取到的新用户名称：" + newUser.getName());
        }
    }
}
