package com.example.demo.mapper;

import com.example.demo.domain.dto.sales.HotProductResponse;
import com.example.demo.domain.dto.sales.SalesRefundResponse;
import com.example.demo.domain.dto.sales.SalesOrderItemResponse;
import com.example.demo.domain.dto.sales.SalesOrderSummaryResponse;
import com.example.demo.domain.entity.SalesOrder;
import com.example.demo.domain.entity.SalesOrderItem;
import com.example.demo.domain.entity.SalesRefund;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SalesOrderMapper {
    // 销售 Mapper：
    // - sales_order：销售主单
    // - sales_order_item：销售明细
    // - sales_refund：退款记录
    @Insert("""
            insert into sales_order(order_no, member_id, total_amount, paid_amount, refund_amount, status, pay_type, cashier_name, created_at)
            values(#{orderNo}, #{memberId}, #{totalAmount}, #{paidAmount}, #{refundAmount}, #{status}, #{payType}, #{cashierName}, now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrder(SalesOrder order);

    @Insert("""
            insert into sales_order_item(order_id, product_id, quantity, unit_price, amount)
            values(#{orderId}, #{productId}, #{quantity}, #{unitPrice}, #{amount})
            """)
    int insertItem(SalesOrderItem item);

    @Select("""
            select coalesce(sum(total_amount), 0)
            from sales_order
            where created_at >= #{startTime} and created_at <= #{endTime}
            """)
    BigDecimal sumSalesAmount(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("""
            select count(1)
            from sales_order
            where created_at >= #{startTime} and created_at <= #{endTime}
            """)
    long countOrders(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("""
            select soi.product_id as productId, p.name as productName, sum(soi.quantity) as totalQuantity
            from sales_order_item soi
            join sales_order so on so.id = soi.order_id
            join product p on p.id = soi.product_id
            where so.created_at >= #{startTime} and so.created_at <= #{endTime}
            group by soi.product_id, p.name
            order by totalQuantity desc
            limit #{limit}
            """)
    List<HotProductResponse> topHotProducts(@Param("startTime") String startTime,
                                            @Param("endTime") String endTime,
                                            @Param("limit") int limit);

    @Select("""
            select id, order_no as orderNo, status, total_amount as totalAmount, paid_amount as paidAmount,
                   pay_type as payType, cashier_name as cashierName, created_at as createdAt
            from sales_order
            order by id desc
            limit #{limit} offset #{offset}
            """)
    List<SalesOrderSummaryResponse> listOrders(@Param("limit") int limit, @Param("offset") int offset);

    @Select("select count(1) from sales_order")
    long countAllOrders();

    @Select("""
            select id, order_no, member_id, total_amount, paid_amount, refund_amount, status, pay_type, cashier_name, created_at
            from sales_order
            where id = #{id}
            limit 1
            """)
    SalesOrder findOrderById(Long id);

    @Select("""
            select soi.product_id as productId, p.name as productName, soi.quantity, soi.unit_price as unitPrice, soi.amount
            from sales_order_item soi
            join product p on p.id = soi.product_id
            where soi.order_id = #{orderId}
            order by soi.id asc
            """)
    List<SalesOrderItemResponse> listItemResponsesByOrderId(Long orderId);

    @Select("select id, order_id, product_id, quantity, unit_price, amount from sales_order_item where order_id = #{orderId}")
    List<SalesOrderItem> listItemsByOrderId(Long orderId);

    @Update("update sales_order set refund_amount = #{refundAmount}, status = #{status} where id = #{id}")
    int updateRefundInfo(@Param("id") Long id, @Param("refundAmount") BigDecimal refundAmount, @Param("status") Integer status);

    @Insert("""
            insert into sales_refund(sales_order_id, refund_no, refund_amount, operator_name, reason, created_at)
            values(#{salesOrderId}, #{refundNo}, #{refundAmount}, #{operatorName}, #{reason}, now())
            """)
    int insertRefund(SalesRefund refund);

    @Select("""
            select id, sales_order_id as salesOrderId, refund_no as refundNo, refund_amount as refundAmount,
                   operator_name as operatorName, reason, created_at as createdAt
            from sales_refund
            order by id desc
            limit #{limit} offset #{offset}
            """)
    List<SalesRefundResponse> listRefunds(@Param("limit") int limit, @Param("offset") int offset);
}
