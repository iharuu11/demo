package com.example.demo.common;

//<T>表示泛型，data的类型可以是任意类型，code为状态码，message为错误信息，data为返回数据
//定义接口的统一返回格式，方便前端处理
public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "ok", data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(0, "ok", null);
    }

    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(-1, message, null);
    }
}
