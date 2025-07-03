package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface DishMapper {
    @Select("select count(id) from dish where category_id=#{id}")
    int countByCategoryId(Long id);

    /**
     * 插入菜品数据
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> page(DishPageQueryDTO dishPageQueryDTO);

    int statusById(Long id);

    /**
     * 根据id删除一条dish
     * @param id
     */
    @Delete("delete from dish where id=#{id};")
    void deleteById(Long id);

    void deleteBatchId(ArrayList<Long> ids);

    @Select("select * from dish where id=#{id}")
    Dish getById(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据菜品类别查菜品
     * @param categoryId
     * @return
     */
    @Select("select * from dish where category_id=#{categoryId}")
    ArrayList<Dish> getListById(Integer categoryId);

    /**
     * 根据id和status动态查询
     * @param dish
     * @return
     */
    List<Dish> list(Dish dish);
}
