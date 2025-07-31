package com.yu.ledger.security;

import com.yu.ledger.entity.po.Users;
import com.yu.ledger.service.IUsersService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 自定义用户详情服务
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final IUsersService usersService;
    
    public CustomUserDetailsService(IUsersService usersService) {
        this.usersService = usersService;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users = usersService.findByUsername(username);
        
        if (users == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        if (users.getStatus() != 1) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }
        
        return new User(users.getUsername(), users.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
} 