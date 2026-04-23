package com.example.demo.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
//全局异常处理器，用于处理所有异常
public class GlobalExceptionHandler {
    //处理IllegalArgumentException异常，返回400状态码
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        // 业务参数/业务规则不满足时，统一用 400 返回给前端：
        // 例如：用户名已存在、库存不足、商品不存在等。
        return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception ex) {
        // DTO 校验失败（@Valid + @NotBlank/@Size 等）会走到这里
        return ResponseEntity.badRequest().body(ApiResponse.fail("request parameters are invalid"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        // 兜底：未预期异常，避免把堆栈信息直接暴露给前端
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("internal server error"));
    }
}
