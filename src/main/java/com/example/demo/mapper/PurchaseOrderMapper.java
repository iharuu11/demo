package com.example.demo.mapper;

import com.example.demo.domain.dto.purchase.PurchaseOrderItemResponse;
import com.example.demo.domain.dto.purchase.PurchaseOrderSummaryResponse;
import com.example.demo.domain.entity.PurchaseOrder;
import com.example.demo.domain.entity.PurchaseOrderItem;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PurchaseOrderMapper {
    // 采购 Mapper：
    // - purchase_order：采购主单
    // - purchase_order_item：采购明细
    // status 约定：0=待入库，1=已入库，2=已取消
    @Insert("""
            insert into purchase_order(order_no, supplier_id, status, total_amount, created_by, created_at)
            values(#{orderNo}, #{supplierId}, #{status}, #{totalAmount}, #{createdBy}, now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrder(PurchaseOrder order);

    @Insert("""
            insert into purchase_order_item(order_id, product_id, quantity, unit_price, amount)
            values(#{orderId}, #{productId}, #{quantity}, #{unitPrice}, #{amount})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertItem(PurchaseOrderItem item);

    @Select("select id, order_no, supplier_id, status, total_amount, created_by, created_at, audited_at from purchase_order where id = #{id} limit 1")
    PurchaseOrder findOrderById(Long id);

    @Select("select id, order_id, product_id, quantity, unit_price, amount from purchase_order_item where order_id = #{orderId}")
    List<PurchaseOrderItem> listItemsByOrderId(Long orderId);

    @Select("""
            select po.id, po.order_no as orderNo, po.supplier_id as supplierId, s.name as supplierName,
                   po.status,
                   case po.status when 0 then 'PENDING' when 1 then 'STOCKED_IN' when 2 then 'CANCELLED' else 'UNKNOWN' end as statusText,
                   po.total_amount as totalAmount, po.created_by as createdBy, po.created_at as createdAt, po.audited_at as auditedAt
            from purchase_order po
            join supplier s on s.id = po.supplier_id
            where (#{status} is null or po.status = #{status})
              and (#{orderNo} is null or po.order_no like concat('%', #{orderNo}, '%'))
              and (#{startTime} is null or po.created_at >= #{startTime})
              and (#{endTime} is null or po.created_at <= #{endTime})
            order by po.id desc
            limit #{limit} offset #{offset}
            """)
    List<PurchaseOrderSummaryResponse> listOrders(@Param("status") Integer status,
                                                  @Param("orderNo") String orderNo,
                                                  @Param("startTime") String startTime,
                                                  @Param("endTime") String endTime,
                                                  @Param("limit") int limit,
                                                  @Param("offset") int offset);

    @Select("""
            select count(1)
            from purchase_order po
            where (#{status} is null or po.status = #{status})
              and (#{orderNo} is null or po.order_no like concat('%', #{orderNo}, '%'))
              and (#{startTime} is null or po.created_at >= #{startTime})
              and (#{endTime} is null or po.created_at <= #{endTime})
            """)
    long countOrders(@Param("status") Integer status,
                     @Param("orderNo") String orderNo,
                     @Param("startTime") String startTime,
                     @Param("endTime") String endTime);

    @Update("update purchase_order set status = #{status}, audited_at = now() where id = #{id} and status = #{expectedStatus}")
    int updateStatus(@Param("id") Long id, @Param("expectedStatus") Integer expectedStatus, @Param("status") Integer status);

    @Update("update purchase_order set status = 2 where id = #{id} and status = 0")
    int cancelOrder(Long id);

    @Select("""
            select coalesce(sum(total_amount), 0)
            from purchase_order
            where created_at >= #{startTime} and created_at <= #{endTime}
            """)
    BigDecimal sumPurchaseAmount(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("""
            select count(1)
            from purchase_order
            where created_at >= #{startTime} and created_at <= #{endTime}
            """)
    long countPurchaseOrders(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("""
            select poi.product_id as productId, p.name as productName, poi.quantity, poi.unit_price as unitPrice, poi.amount
            from purchase_order_item poi
            join product p on p.id = poi.product_id
            where poi.order_id = #{orderId}
            order by poi.id asc
            """)
    List<PurchaseOrderItemResponse> listOrderItemResponses(Long orderId);
}
