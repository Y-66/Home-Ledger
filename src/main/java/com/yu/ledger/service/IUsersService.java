package com.yu.ledger.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.ledger.entity.po.Users;

/**
 * 用户服务接口
 */
public interface IUsersService extends IService<Users> {
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户信息
     */
    Users findByUsername(String username);
    
    /**
     * 用户注册
     * @param users 用户信息
     * @return 是否成功
     */
    boolean register(Users users);
} 