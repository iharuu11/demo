package com.example.demo.domain.entity;

import java.time.LocalDateTime;

//角色实体类
public class Role {
    private Long id; //角色id
    private String code; //角色编码
    private String name; //角色名称
    private Integer status; //角色状态
    private LocalDateTime createdAt; //创建时间

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
