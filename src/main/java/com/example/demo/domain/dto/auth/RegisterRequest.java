package com.example.demo.domain.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//纯数据传输对象（DTO）只负责“搬运数据”。实体对象（Entity）有“业务身份和生命周期”
// 注册请求DTO，record能简化代码，自动生成构造函数，getters，setters，equals，hashCode，toString等方法
public record RegisterRequest(
        //notblank用于校验字符串是否为空，size用于校验字符串长度
        @NotBlank(message = "username is required")
        @Size(min = 4, max = 32, message = "username length must be 4-32")
        String username,//用户名，必填，长度为4-32
        @NotBlank(message = "password is required")//密码，必填，长度为6-64
        @Size(min = 6, max = 64, message = "password length must be 6-64")
        String password//密码
) {
}
