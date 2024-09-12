package com.example.common.service;

import com.example.common.model.User;

/**
 * 用户接口类
 */
public interface UserService {
    /**
     * 获取用户姓名服务
     * @param user
     * @return
     */
    User getUser(User user);

    /**
     * 新方法-获取数字
     * @return
     */
    default short getNumber() {
        return 666;
    }
}
