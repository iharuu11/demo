package com.example.demo.mapper;

import com.example.demo.domain.entity.Category;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CategoryMapper {
    @Insert("""
            insert into category(name, status, created_at)
            values(#{name}, #{status}, now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Select("select id, name, status, created_at from category order by id desc")
    List<Category> listAll();

    @Select("select id, name, status, created_at from category where id = #{id} limit 1")
    Category findById(Long id);

    @Update("""
            update category
            set name = #{name}, status = #{status}
            where id = #{id}
            """)
    int update(Category category);

    @Delete("delete from category where id = #{id}")
    int deleteById(Long id);
}
