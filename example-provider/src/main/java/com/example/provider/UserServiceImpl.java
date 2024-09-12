package com.example.provider;

import com.example.common.model.User;
import com.example.common.service.UserService;

public class UserServiceImpl implements UserService {
    /**
     * 用户服务实现类
     * @param user
     * @return
     */
    @Override
    public User getUser(User user) {
        System.out.println("用户姓名："+user.getName());
        return user;
    }
}
