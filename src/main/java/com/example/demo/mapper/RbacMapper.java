package com.example.demo.mapper;

import com.example.demo.domain.entity.Permission;
import com.example.demo.domain.entity.Role;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RbacMapper {
    //插入角色
    @Insert("insert into sys_role(code, name, status, created_at) values(#{code}, #{name}, #{status}, now())")
    //使用GeneratedKeys自动生成id
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRole(Role role);//返回插入的行数

    //插入权限
    @Insert("insert into sys_permission(code, name, type, created_at) values(#{code}, #{name}, #{type}, now())")
    //使用GeneratedKeys自动生成id
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertPermission(Permission permission);
    //按id查询角色
    @Select("select id, code, name, status, created_at from sys_role where id = #{id} limit 1")
    Role findRoleById(Long id);
    //按id查询权限
    @Select("select id, code, name, type, created_at from sys_permission where id = #{id} limit 1")
    Permission findPermissionById(Long id);
    //按编码查询角色
    @Select("select id, code, name, status, created_at from sys_role where code = #{code} limit 1")
    Role findRoleByCode(String code);
    @Select("select id, code, name, status, created_at from sys_role order by id asc")
    List<Role> listRoles();
    @Select("select id, code, name, type, created_at from sys_permission order by id asc")
    List<Permission> listPermissions();
    @Select("select permission_id from sys_role_permission where role_id = #{roleId}")
    List<Long> listRolePermissionIds(Long roleId);
    //插入角色权限
    @Insert("insert ignore into sys_role_permission(role_id, permission_id) values(#{roleId}, #{permissionId})")
    int assignPermissionToRole(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
    @Delete("delete from sys_role_permission where role_id = #{roleId}")
    int deleteRolePermissions(Long roleId);
    //删除用户角色
    @Delete("delete from sys_user_role where user_id = #{userId}")
    int deleteUserRoles(Long userId);
    //插入用户角色
    @Insert("insert into sys_user_role(user_id, role_id) values(#{userId}, #{roleId})")
    int assignUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
    //查询用户权限
    @Select("""
            select p.code
            from sys_permission p
            join sys_role_permission rp on rp.permission_id = p.id
            join sys_user_role ur on ur.role_id = rp.role_id
            where ur.user_id = #{userId}
            """)
    List<String> listUserPermissionCodes(Long userId);
}
