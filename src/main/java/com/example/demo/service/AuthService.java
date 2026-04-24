package com.example.demo.service;

import com.example.demo.domain.dto.auth.LoginRequest;
import com.example.demo.domain.dto.auth.LoginResponse;
import com.example.demo.domain.dto.auth.RegisterRequest;
import com.example.demo.domain.dto.auth.CurrentUserPermissionsResponse;
import com.example.demo.domain.entity.Role;
import com.example.demo.domain.entity.User;
import com.example.demo.mapper.RbacMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.security.JwtTokenProvider;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//@service用于标注该类为Spring的Service层，用于业务逻辑处理,声明为Spring的bean，由Spring容器管理
@Service
//AuthService类，用于处理用户注册和登录等认证相关业务逻辑
public class AuthService {
    // 注入UserMapper，用于操作用户表
    private final UserMapper userMapper;
    // 注入RbacMapper，用于操作用户角色权限表
    private final RbacMapper rbacMapper;
    // 注入PasswordEncoder，用于加密密码
    private final PasswordEncoder passwordEncoder;
    // 注入JwtTokenProvider，用于生成JWT Token
    private final JwtTokenProvider jwtTokenProvider;
    // 构造函数，用于注入UserMapper, RbacMapper, PasswordEncoder, JwtTokenProvider
    public AuthService(UserMapper userMapper, RbacMapper rbacMapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper; 
        this.rbacMapper = rbacMapper; 
        this.passwordEncoder = passwordEncoder; 
        this.jwtTokenProvider = jwtTokenProvider; 
    }

    // 注册方法，用于注册新用户
    //@transactional用于事务管理，确保注册过程要么全部成功，要么全部回滚
    @Transactional
    public void register(RegisterRequest request) {
        // 注册逻辑（写数据库，所以加事务）：
        // - 同名用户名不允许重复
        // - 密码必须加密存储（BCrypt）
        // - 新用户默认 STAFF 角色 + 启用状态
        User existed = userMapper.findByUsername(request.username());
        if (existed != null) {
            throw new IllegalArgumentException("username already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        //加密密码
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("TOURIST");
        user.setStatus(1);
        userMapper.insert(user);

        // 绑定 RBAC 角色（如果系统中配置了 STAFF 角色）
        //默认将新用户绑定到STAFF角色
        Role staffRole = rbacMapper.findRoleByCode("STAFF");
        if (staffRole != null) {
            rbacMapper.assignUserRole(user.getId(), staffRole.getId());
        }
    }

    public LoginResponse login(LoginRequest request) {
        // 登录逻辑：
        // 1) 按用户名查用户
        // 2) 用 BCrypt 校验密码是否匹配（不能用明文对比）
        // 3) 校验账号是否启用
        // 4) 查询权限列表，生成 JWT Token 并返回给前端
        User user = userMapper.findByUsername(request.username());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("username or password is invalid");
        }
        //校验账号是否启用，status为1表示启用，0表示禁用
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("user is disabled");
        }
        List<String> permissions = rbacMapper.listUserPermissionCodes(user.getId());
        String token = jwtTokenProvider.createToken(user.getUsername(), user.getRole(), permissions);
        return new LoginResponse(token, "Bearer", user.getUsername(), user.getRole(), permissions);
    }

    //查询当前登录用户的权限信息并返回给前端
    public CurrentUserPermissionsResponse currentUserPermissions(String username) {
        // 用于“刷新页面后恢复权限”的接口：
        // 前端只有 token，但 pinia 状态会丢失，所以需要再拉一次权限列表
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        List<String> permissions = rbacMapper.listUserPermissionCodes(user.getId());
        return new CurrentUserPermissionsResponse(user.getUsername(), user.getRole(), permissions);
    }
}
