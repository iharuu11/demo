package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.domain.dto.member.CreateMemberRequest;
import com.example.demo.domain.dto.member.MemberResponse;
import com.example.demo.service.MemberService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PostMapping
    public ApiResponse<MemberResponse> create(@Valid @RequestBody CreateMemberRequest request) {
        // 新增会员（需要登录，且角色为 ADMIN/STAFF 才能操作）：
        // - @Valid 会触发 CreateMemberRequest 上的手机号/姓名等校验
        // - 创建成功后返回会员信息（含 id、余额、积分等）
        return ApiResponse.success(memberService.create(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping
    public ApiResponse<List<MemberResponse>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 分页查询会员列表：
        // - pageNum 从 1 开始
        // - pageSize 会在 service 里做安全限制，避免一次查太多
        return ApiResponse.success(memberService.list(pageNum, pageSize));
    }
}
