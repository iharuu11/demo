package com.example.demo.mapper;

import com.example.demo.domain.entity.Member;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MemberMapper {
    // MyBatis Mapper：这里用注解方式写 SQL
    // - findByPhone: 按手机号查询会员（用于创建前查重）
    // - insert: 插入会员记录（useGeneratedKeys 会把数据库生成的 id 回填到 member.id）
    // - listMembers: 分页列表查询
    // - countMembers: 统计总数（可用于分页总数展示）
    @Select("select id, phone, name, gender, level, points, balance, status, created_at, updated_at from member where phone = #{phone} limit 1")
    Member findByPhone(String phone);

    @Insert("""
            insert into member (phone, name, gender, level, points, balance, status, created_at, updated_at)
            values (#{phone}, #{name}, #{gender}, #{level}, #{points}, #{balance}, #{status}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Member member);

    @Select("""
            select id, phone, name, gender, level, points, balance, status, created_at, updated_at
            from member
            order by id desc
            limit #{limit} offset #{offset}
            """)
    List<Member> listMembers(@Param("limit") int limit, @Param("offset") int offset);

    @Select("select count(1) from member")
    long countMembers();
}
