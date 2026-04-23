package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.domain.dto.auth.CurrentUserPermissionsResponse;
import com.example.demo.domain.dto.auth.LoginRequest;
import com.example.demo.domain.dto.auth.LoginResponse;
import com.example.demo.domain.dto.auth.RegisterRequest;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//"/api"表示这是后端的api接口，与前端路由界面做区分
@RequestMapping("/api/auth")
public class AuthController {
    // 注入AuthService，用于处理注册和登录请求
    private final AuthService authService;

    // 构造函数，用于注入AuthService
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register") //将http请求映射到方法上，(在网页上请求/register时就会调用以下的register方法)
    //@valid用于校验请求参数，与@notblank，@size等注解配合使用，@requestbody用于将前端传来的json数据绑定到方法参数上
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        // 注册接口（公开接口，无需登录）：
        // 1) 校验参数（@Valid：用户名/密码不能为空且长度符合要求）
        // 2) 创建用户并写入数据库（密码会加密存储）
        authService.register(request);
        return ApiResponse.success();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 登录接口（公开接口，无需登录）：
        // - 校验用户名/密码正确后，返回 JWT Token 与用户权限信息
        return ApiResponse.success(authService.login(request));
    }

    //@preauthorize用于校验用户是否登录，isauthenticated()表示用户必须登录
    //在访问/api/auth/me/permissions时，会先进行权限校验，如果用户未登录，则返回401错误
    //前面/register和/login都是公开接口，不需要登录，所以不需要进行权限校验
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/permissions")
    public ApiResponse<CurrentUserPermissionsResponse> currentUserPermissions(Authentication authentication) {
        // 获取当前登录用户的权限列表：
        // - Authentication 来自 JWT 过滤器解析出来的“当前用户身份”
        return ApiResponse.success(authService.currentUserPermissions(authentication.getName()));
    }
}
