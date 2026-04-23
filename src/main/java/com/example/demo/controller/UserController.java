package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.domain.dto.user.UpdateUserStatusRequest;
import com.example.demo.domain.dto.user.UserPageResponse;
import com.example.demo.domain.dto.user.UserSummaryResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    //展示当前登录用户基本信息
    public ApiResponse<UserSummaryResponse> me(Authentication authentication) {
        return ApiResponse.success(userService.getByUsername(authentication.getName()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<UserPageResponse> listUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(userService.listUsers(pageNum, pageSize));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        userService.updateUserStatus(id, request.status());
        return ApiResponse.success();
    }
}
