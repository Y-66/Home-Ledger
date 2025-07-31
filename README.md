# Ledger - Spring Boot 项目

这是一个基于Spring Boot的账本管理系统，集成了Spring Security进行安全认证。

## 技术栈

- Spring Boot 3.4.8
- Spring Security
- MyBatis Plus
- MySQL
- JWT
- Swagger

## 项目结构

```
src/main/java/com/yu/ledger/
├── config/                 # 配置类
│   └── SecurityConfig.java # Spring Security配置
├── controller/             # 控制器
│   ├── AuthController.java # 认证控制器
│   └── ...                 # 其他业务控制器
├── entity/po/              # 实体类
│   ├── Users.java          # 用户实体
│   └── ...                 # 其他业务实体
├── mapper/                 # MyBatis映射器
│   ├── UsersMapper.java    # 用户映射器
│   └── ...                 # 其他业务映射器
├── security/               # 安全相关组件
│   ├── JwtAuthenticationEntryPoint.java    # JWT认证入口点
│   ├── JwtAuthenticationFilter.java        # JWT认证过滤器
│   ├── JwtTokenProvider.java               # JWT令牌提供者
│   └── CustomUserDetailsService.java       # 自定义用户详情服务
├── service/                # 服务层
│   ├── IUsersService.java  # 用户服务接口
│   ├── impl/               # 服务实现
│   │   ├── UsersServiceImpl.java
│   │   └── ...
│   └── ...                 # 其他业务服务
└── LedgerApplication.java  # 启动类
```

## 功能特性

### 1. 用户认证
- 用户注册
- 用户登录
- JWT令牌认证
- 密码加密存储

### 2. 安全配置
- 基于JWT的无状态认证
- 密码加密（BCrypt）
- CORS跨域支持
- 方法级安全注解支持

### 3. API接口
- `/api/auth/login` - 用户登录
- `/api/auth/register` - 用户注册
- `/api/auth/me` - 获取当前用户信息

## 快速开始

### 1. 数据库配置

1. 创建MySQL数据库：
```sql
CREATE DATABASE ledger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行SQL脚本：
```sql
-- 执行 src/main/resources/sql/users.sql
```

### 2. 配置文件

修改 `src/main/resources/application.yaml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ledger?serverTimezone=Asia/Shanghai&useSSL=false&useUnicode=true&characterEncoding=utf-8
    username: your_username
    password: your_password
```

### 3. JWT配置

修改JWT密钥（生产环境请使用强密钥）：

```yaml
app:
  jwt:
    secret: your-secret-key-here-make-it-long-and-secure-at-least-256-bits
    expiration: 86400000  # 24小时
```

### 4. 启动应用

```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

## API使用示例

### 1. 用户登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

响应：
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "admin"
}
```

### 2. 使用JWT令牌访问受保护的资源

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

### 3. 用户注册

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "123456",
    "realName": "新用户",
    "email": "newuser@example.com",
    "phone": "13800138002"
  }'
```

## 安全说明

1. **密码加密**：使用BCrypt算法对密码进行加密存储
2. **JWT令牌**：使用HMAC-SHA512算法签名，确保令牌安全性
3. **无状态认证**：基于JWT的无状态认证，支持分布式部署
4. **CORS支持**：配置了跨域资源共享，支持前端跨域访问

## 测试账号

系统预置了两个测试账号：

- 用户名：`admin`，密码：`123456`
- 用户名：`user`，密码：`123456`

## 注意事项

1. 生产环境请修改JWT密钥
2. 建议配置HTTPS
3. 定期更新依赖版本
4. 监控JWT令牌过期时间 