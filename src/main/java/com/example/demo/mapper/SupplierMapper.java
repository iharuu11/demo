package com.example.demo.mapper;

import com.example.demo.domain.entity.Supplier;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SupplierMapper {
    // 供应商 Mapper：
    // - supplier 表保存供应商基础信息
    // - 提供新增、按 id 查询、分页查询、更新、状态更新等能力
    @Insert("""
            insert into supplier(name, contact_name, contact_phone, address, status, created_at)
            values(#{name}, #{contactName}, #{contactPhone}, #{address}, #{status}, now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Supplier supplier);

    @Select("select id, name, contact_name, contact_phone, address, status, created_at from supplier where id = #{id} limit 1")
    Supplier findById(Long id);

    @Select("select id, name, contact_name, contact_phone, address, status, created_at from supplier order by id desc")
    List<Supplier> listAll();

    @Select("""
            select id, name, contact_name, contact_phone, address, status, created_at
            from supplier
            where (
              #{keyword} is null
              or name like concat('%', #{keyword}, '%')
              or contact_name like concat('%', #{keyword}, '%')
              or contact_phone like concat('%', #{keyword}, '%')
            )
            order by id desc
            limit #{limit} offset #{offset}
            """)
    List<Supplier> listPaged(@Param("keyword") String keyword, @Param("limit") int limit, @Param("offset") int offset);

    @Update("""
            update supplier
            set name = #{name}, contact_name = #{contactName}, contact_phone = #{contactPhone}, address = #{address}
            where id = #{id}
            """)
    int update(Supplier supplier);

    @Update("update supplier set status = #{status} where id = #{id}")
    int updateStatus(Supplier supplier);
}
