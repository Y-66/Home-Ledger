package com.yu.ledger.controller;

import com.yu.ledger.entity.po.Users;
import com.yu.ledger.service.IUsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试控制器 - 用于验证Spring Security配置
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "测试接口", description = "用于验证Spring Security配置的测试接口")
public class TestController {
    
    private final IUsersService usersService;
    
    public TestController(IUsersService usersService) {
        this.usersService = usersService;
    }
    
    @GetMapping("/public")
    @Operation(summary = "公开接口", description = "无需认证的公开接口")
    public ResponseEntity<Map<String, Object>> publicEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "这是一个公开接口，无需认证");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/protected")
    @Operation(summary = "受保护接口", description = "需要JWT认证的受保护接口")
    public ResponseEntity<Map<String, Object>> protectedEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "这是一个受保护的接口，认证成功！");
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/admin")
    @Operation(summary = "管理员接口", description = "需要管理员角色的接口")
    public ResponseEntity<Map<String, Object>> adminEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "这是管理员接口，访问成功！");
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/db-test")
    @Operation(summary = "数据库测试", description = "测试数据库连接和用户数据")
    public ResponseEntity<Map<String, Object>> dbTest() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 测试查询所有用户
            List<Users> users = usersService.list();
            response.put("success", true);
            response.put("message", "数据库连接成功");
            response.put("userCount", users.size());
            response.put("users", users);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "数据库连接失败: " + e.getMessage());
            response.put("error", e.toString());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/admin")
    @Operation(summary = "查询admin用户", description = "查询admin用户的详细信息")
    public ResponseEntity<Map<String, Object>> getAdminUser() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Users adminUser = usersService.findByUsername("admin");
            if (adminUser != null) {
                response.put("success", true);
                response.put("message", "找到admin用户");
                response.put("user", adminUser);
            } else {
                response.put("success", false);
                response.put("message", "未找到admin用户");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            response.put("error", e.toString());
        }
        
        return ResponseEntity.ok(response);
    }
} 