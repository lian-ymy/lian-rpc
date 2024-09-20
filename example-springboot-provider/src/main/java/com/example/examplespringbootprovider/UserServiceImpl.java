package com.example.examplespringbootprovider;

import com.example.common.model.User;
import com.example.common.service.UserService;
import com.example.rpc.springboot.starter.annotation.RpcService;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Service
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("用户名："+user.getName() );
        return user;
    }
}
