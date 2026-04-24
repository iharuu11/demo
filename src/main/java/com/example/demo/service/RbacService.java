package com.example.demo.service;

import com.example.demo.domain.entity.Permission;
import com.example.demo.domain.entity.Role;
import com.example.demo.domain.entity.User;
import com.example.demo.mapper.RbacMapper;
import com.example.demo.mapper.UserMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RbacService {
    private final RbacMapper rbacMapper;
    private final UserMapper userMapper;

    public RbacService(RbacMapper rbacMapper, UserMapper userMapper) {
        this.rbacMapper = rbacMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public Long createRole(String code, String name) {
        if (rbacMapper.findRoleByCode(code) != null) {
            throw new IllegalArgumentException("role code already exists");
        }
        Role role = new Role();
        //code转换为大写
        role.setCode(code.toUpperCase());
        role.setName(name);
        role.setStatus(1);
        rbacMapper.insertRole(role);
        return role.getId();
    }

    @Transactional
    public Long createPermission(String code, String name, String type) {
        Permission permission = new Permission();
        permission.setCode(code);
        permission.setName(name);
        permission.setType(type);
        rbacMapper.insertPermission(permission);
        return permission.getId();
    }

    public List<Role> listRoles() {
        return rbacMapper.listRoles();
    }

    public List<Permission> listPermissions() {
        return rbacMapper.listPermissions();
    }

    public List<Long> listRolePermissionIds(Long roleId) {
        if (rbacMapper.findRoleById(roleId) == null) {
            throw new IllegalArgumentException("role not found");
        }
        return rbacMapper.listRolePermissionIds(roleId);
    }

    @Transactional
    public void assignPermissionToRole(Long roleId, Long permissionId) {
        if (rbacMapper.findRoleById(roleId) == null) {
            throw new IllegalArgumentException("role not found");
        }
        if (rbacMapper.findPermissionById(permissionId) == null) {
            throw new IllegalArgumentException("permission not found");
        }
        rbacMapper.assignPermissionToRole(roleId, permissionId);
    }

    @Transactional
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        if (rbacMapper.findRoleById(roleId) == null) {
            throw new IllegalArgumentException("role not found");
        }
        rbacMapper.deleteRolePermissions(roleId);
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        for (Long permissionId : permissionIds) {
            if (permissionId == null || rbacMapper.findPermissionById(permissionId) == null) {
                throw new IllegalArgumentException("permission not found: " + permissionId);
            }
            rbacMapper.assignPermissionToRole(roleId, permissionId);
        }
    }

    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        Role role = rbacMapper.findRoleById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("role not found");
        }
        rbacMapper.deleteUserRoles(userId);
        rbacMapper.assignUserRole(userId, roleId);
        userMapper.updateRole(userId, role.getCode());
    }
}
