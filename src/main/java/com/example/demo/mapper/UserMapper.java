package com.example.demo.mapper;

import com.example.demo.domain.entity.User;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    @Select("select id, username, password, role, status, created_at, updated_at from sys_user where username = #{username} limit 1")
    User findByUsername(String username);

    @Insert("""
            insert into sys_user (username, password, role, status, created_at, updated_at)
            values (#{username}, #{password}, #{role}, #{status}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("select id, username, password, role, status, created_at, updated_at from sys_user where id = #{id} limit 1")
    User findById(Long id);

    @Select("""
            select id, username, password, role, status, created_at, updated_at
            from sys_user
            order by id desc
            limit #{limit} offset #{offset}
            """)
    List<User> listUsers(@Param("limit") int limit, @Param("offset") int offset);

    @Select("select count(1) from sys_user")
    long countUsers();

    @Update("update sys_user set status = #{status}, updated_at = now() where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("update sys_user set role = #{role}, updated_at = now() where id = #{id}")
    int updateRole(@Param("id") Long id, @Param("role") String role);
}
