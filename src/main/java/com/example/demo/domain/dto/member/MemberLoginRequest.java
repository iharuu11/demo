package com.example.demo.domain.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberLoginRequest(
        @NotBlank(message = "phone is required")
        @Pattern(regexp = "^1\\d{10}$", message = "phone format is invalid")
        String phone,
        @NotBlank(message = "password is required")
        @Size(min = 6, max = 32, message = "password length must be 6~32")
        String password
) {
}
