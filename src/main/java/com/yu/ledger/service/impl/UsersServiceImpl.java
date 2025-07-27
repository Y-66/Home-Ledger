package com.yu.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.ledger.entity.po.Users;
import com.yu.ledger.mapper.UsersMapper;
import com.yu.ledger.service.IUsersService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {
    
    private final PasswordEncoder passwordEncoder;
    
    public UsersServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public Users findByUsername(String username) {
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUsername, username);
        return this.getOne(wrapper);
    }
    
    @Override
    public boolean register(Users users) {
        // 检查用户名是否已存在
        if (findByUsername(users.getUsername()) != null) {
            return false;
        }
        
        // 加密密码
        users.setPassword(passwordEncoder.encode(users.getPassword()));
        users.setStatus(1); // 默认启用
        users.setCreateTime(LocalDateTime.now());
        users.setUpdateTime(LocalDateTime.now());
        
        return this.save(users);
    }
} 