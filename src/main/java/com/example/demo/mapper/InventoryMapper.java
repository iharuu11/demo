package com.example.demo.mapper;

import com.example.demo.domain.dto.product.InventoryInfoResponse;
import com.example.demo.domain.dto.product.InventoryLogResponse;
import com.example.demo.domain.dto.dashboard.InventoryWarningResponse;
import com.example.demo.domain.entity.Inventory;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InventoryMapper {
    // 库存 Mapper（MyBatis 注解 SQL）：
    // - inventory 表：保存每个商品当前库存 quantity 与预警值 warning_qty
    // - inventory_log 表：保存每次库存变更的流水（delta、变更后数量、业务类型、操作人等）
    @Select("select product_id, quantity, warning_qty, updated_at from inventory where product_id = #{productId} limit 1")
    Inventory findByProductId(Long productId);

    @Insert("insert into inventory(product_id, quantity, warning_qty, updated_at) values(#{productId}, #{quantity}, #{warningQty}, now())")
    int insert(Inventory inventory);

    @Update("update inventory set quantity = quantity + #{deltaQty}, updated_at = now() where product_id = #{productId}")
    int addQuantity(@Param("productId") Long productId, @Param("deltaQty") Integer deltaQty);

    @Update("update inventory set warning_qty = #{warningQty}, updated_at = now() where product_id = #{productId}")
    int updateWarningQty(@Param("productId") Long productId, @Param("warningQty") Integer warningQty);

    @Insert("""
            insert into inventory_log(product_id, delta_qty, after_qty, biz_type, remark, operator_name, created_at)
            values(#{productId}, #{deltaQty}, #{afterQty}, #{bizType}, #{remark}, #{operatorName}, now())
            """)
    int insertLog(@Param("productId") Long productId,
                  @Param("deltaQty") Integer deltaQty,
                  @Param("afterQty") Integer afterQty,
                  @Param("bizType") String bizType,
                  @Param("remark") String remark,
                  @Param("operatorName") String operatorName);

    @Select("""
            select i.product_id as productId, p.name as productName, i.quantity, i.warning_qty as warningQty
            from inventory i
            join product p on p.id = i.product_id
            where i.quantity <= i.warning_qty
            order by i.quantity asc, i.product_id asc
            limit #{limit} offset #{offset}
            """)
    List<InventoryWarningResponse> listWarnings(@Param("limit") int limit, @Param("offset") int offset);

    @Select("select count(1) from inventory where quantity <= warning_qty")
    long countWarnings();

    @Select("""
            select i.product_id as productId, p.name as productName, i.quantity, i.warning_qty as warningQty
            from inventory i
            join product p on p.id = i.product_id
            where (#{keyword} is null or p.name like concat('%', #{keyword}, '%') or cast(i.product_id as char) like concat('%', #{keyword}, '%'))
            order by i.product_id desc
            limit #{limit} offset #{offset}
            """)
    List<InventoryInfoResponse> listInventory(@Param("keyword") String keyword, @Param("limit") int limit, @Param("offset") int offset);

    @Select("""
            select count(1)
            from inventory i
            join product p on p.id = i.product_id
            where (#{keyword} is null or p.name like concat('%', #{keyword}, '%') or cast(i.product_id as char) like concat('%', #{keyword}, '%'))
            """)
    long countInventory(@Param("keyword") String keyword);

    //查询库存流水，productId为空时查询所有，否则查询指定商品的流水
    @Select("""
            select il.id, il.product_id as productId, p.name as productName, il.delta_qty as deltaQty, il.after_qty as afterQty,
                   il.biz_type as bizType, il.remark, il.operator_name as operatorName, il.created_at as createdAt
            from inventory_log il
            join product p on p.id = il.product_id
            where (#{productId} is null or il.product_id = #{productId})
            order by il.id desc
            limit #{limit} offset #{offset}
            """)
    List<InventoryLogResponse> listLogs(@Param("productId") Long productId, @Param("limit") int limit, @Param("offset") int offset);

    @Select("""
            select count(1)
            from inventory_log il
            where (#{productId} is null or il.product_id = #{productId})
            """)
    long countLogs(@Param("productId") Long productId);

    @Delete("delete from inventory where product_id = #{productId}")
    int deleteByProductId(Long productId);
}
