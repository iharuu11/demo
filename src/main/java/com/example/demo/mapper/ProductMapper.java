package com.example.demo.mapper;

import com.example.demo.domain.entity.Product;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper {
    @Select("select id, name, barcode, category_id, purchase_price, sale_price, status, created_at from product where barcode = #{barcode} limit 1")
    Product findByBarcode(String barcode);

    @Select("select id, name, barcode, category_id, purchase_price, sale_price, status, created_at from product where barcode = #{barcode} and id <> #{id} limit 1")
    Product findByBarcodeExcludeId(@Param("barcode") String barcode, @Param("id") Long id);

    @Select("select id, name, barcode, category_id, purchase_price, sale_price, status, created_at from product where id = #{id} limit 1")
    Product findById(Long id);

    @Insert("""
            insert into product(name, barcode, category_id, purchase_price, sale_price, status, created_at)
            values(#{name}, #{barcode}, #{categoryId}, #{purchasePrice}, #{salePrice}, #{status}, now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Select("""
            select id, name, barcode, category_id, purchase_price, sale_price, status, created_at
            from product
            where (#{keyword} is null or name like concat('%', #{keyword}, '%') or barcode like concat('%', #{keyword}, '%'))
            order by id desc
            limit #{limit} offset #{offset}
            """)
    List<Product> list(@Param("keyword") String keyword, @Param("limit") int limit, @Param("offset") int offset);

    @Select("""
            select count(1)
            from product
            where (#{keyword} is null or name like concat('%', #{keyword}, '%') or barcode like concat('%', #{keyword}, '%'))
            """)
    long count(@Param("keyword") String keyword);

    @Update("""
            update product
            set name = #{name}, barcode = #{barcode}, category_id = #{categoryId},
                purchase_price = #{purchasePrice}, sale_price = #{salePrice}
            where id = #{id}
            """)
    int update(Product product);

    @Update("update product set status = #{status} where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("delete from product where id = #{id}")
    int deleteById(Long id);

    @Select("select count(1) from product where category_id = #{categoryId}")
    long countByCategoryId(Long categoryId);
}
