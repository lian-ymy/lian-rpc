package com.example.consumer;

import com.example.common.model.User;
import com.example.common.service.UserService;
import com.example.proxy.ServiceProxyFactory;

/**
 * 示例服务消费者
 */
public class EasyConsumerExample {
    public static void main(String[] args) {
        // todo 提供服务接口类的实例
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("镜流");
        User newUser = userService.getUser(user);
        if (newUser == null) {
            System.out.println("newUser == null!");
        } else {
            System.out.println("获取到的newUser为："+newUser.getName());
        }
    }
}
