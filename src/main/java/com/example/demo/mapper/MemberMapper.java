package com.example.demo.mapper;

import com.example.demo.domain.dto.member.MemberBalanceLogResponse;
import com.example.demo.domain.entity.Member;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MemberMapper {
    // MyBatis Mapper：这里用注解方式写 SQL
    // - findByPhone: 按手机号查询会员（用于创建前查重）
    // - insert: 插入会员记录（useGeneratedKeys 会把数据库生成的 id 回填到 member.id）
    // - listMembers: 分页列表查询
    // - countMembers: 统计总数（可用于分页总数展示）
    @Select("select id, phone, password, name, gender, level, points, balance, status, created_at, updated_at from member where phone = #{phone} limit 1")
    Member findByPhone(String phone);

    @Select("select id, phone, password, name, gender, level, points, balance, status, created_at, updated_at from member where id = #{id} limit 1")
    Member findById(Long id);

    @Insert("""
            insert into member (phone, password, name, gender, level, points, balance, status, created_at, updated_at)
            values (#{phone}, #{password}, #{name}, #{gender}, #{level}, #{points}, #{balance}, #{status}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Member member);

    @Select("""
            select id, phone, password, name, gender, level, points, balance, status, created_at, updated_at
            from member
            where (#{keyword} is null
                or phone like concat('%', #{keyword}, '%')
                or name like concat('%', #{keyword}, '%'))
            order by id desc
            limit #{limit} offset #{offset}
            """)
    List<Member> listMembers(@Param("keyword") String keyword, @Param("limit") int limit, @Param("offset") int offset);

    @Update("""
            update member
            set name = #{name},
                gender = #{gender},
                level = #{level},
                points = #{points},
                balance = #{balance},
                updated_at = now()
            where id = #{id}
            """)
    int update(Member member);

    @Update("""
            update member
            set status = #{status},
                updated_at = now()
            where id = #{id}
            """)
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("""
            update member
            set balance = #{balance},
                points = #{points},
                level = #{level},
                updated_at = now()
            where id = #{id}
            """)
    int updateBenefits(Member member);

    @Insert("""
            insert into member_balance_log(member_id, delta_amount, after_balance, biz_type, remark, operator_name, created_at)
            values(#{memberId}, #{deltaAmount}, #{afterBalance}, #{bizType}, #{remark}, #{operatorName}, now())
            """)
    int insertBalanceLog(@Param("memberId") Long memberId,
                         @Param("deltaAmount") java.math.BigDecimal deltaAmount,
                         @Param("afterBalance") java.math.BigDecimal afterBalance,
                         @Param("bizType") String bizType,
                         @Param("remark") String remark,
                         @Param("operatorName") String operatorName);

    @Select("""
            select l.id,
                   l.member_id as memberId,
                   m.phone as memberPhone,
                   m.name as memberName,
                   l.delta_amount as deltaAmount,
                   l.after_balance as afterBalance,
                   l.biz_type as bizType,
                   l.remark,
                   l.operator_name as operatorName,
                   l.created_at as createdAt
            from member_balance_log l
            join member m on m.id = l.member_id
            where (#{keyword} is null
                or m.phone like concat('%', #{keyword}, '%')
                or m.name like concat('%', #{keyword}, '%'))
              and (#{bizType} is null or l.biz_type = #{bizType})
            order by l.id desc
            limit #{limit} offset #{offset}
            """)
    List<MemberBalanceLogResponse> listBalanceLogs(@Param("keyword") String keyword,
                                                   @Param("bizType") String bizType,
                                                   @Param("limit") int limit,
                                                   @Param("offset") int offset);

    @Select("""
            select count(1)
            from member_balance_log l
            join member m on m.id = l.member_id
            where (#{keyword} is null
                or m.phone like concat('%', #{keyword}, '%')
                or m.name like concat('%', #{keyword}, '%'))
              and (#{bizType} is null or l.biz_type = #{bizType})
            """)
    long countBalanceLogs(@Param("keyword") String keyword, @Param("bizType") String bizType);

    @Select("""
            select count(1)
            from member
            where (#{keyword} is null
                or phone like concat('%', #{keyword}, '%')
                or name like concat('%', #{keyword}, '%'))
            """)
    long countMembers(@Param("keyword") String keyword);
}
