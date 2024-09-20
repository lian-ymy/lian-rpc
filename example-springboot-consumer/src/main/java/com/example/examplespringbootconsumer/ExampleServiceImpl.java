package com.example.examplespringbootconsumer;

import com.example.common.model.User;
import com.example.common.service.UserService;
import com.example.rpc.springboot.starter.annotation.RpcReference;
import org.springframework.stereotype.Service;

@Service
public class ExampleServiceImpl {
    @RpcReference
    private UserService userService;

    public void test() {
        User user = new User();
        user.setName("镜流");
        User registerUser = userService.getUser(user);
        System.out.println(registerUser.getName());
    }
}
