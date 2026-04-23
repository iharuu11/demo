package com.example.demo.domain.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateMemberRequest(
        // 手机号：必填，且必须是 11 位、以 1 开头（简单校验国内手机号格式）
        @NotBlank(message = "phone is required")
        @Pattern(regexp = "^1\\d{10}$", message = "phone format is invalid")
        String phone,
        // 姓名：必填，长度不超过 32
        @NotBlank(message = "name is required")
        @Size(max = 32, message = "name length must be <= 32")
        String name,
        // 性别：可选（由前端传或不传）
        Integer gender
) {
}
