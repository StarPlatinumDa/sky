package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入口味数据 动态sql
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据dishId删除口味表
     * @param id
     */
    @Delete("delete from dish_flavor where dish_id=#{id};")
    void deleteByDishId(Long id);

    void deleteBatchId(ArrayList<Long> ids);

    @Select("select * from dish_flavor where dish_id=#{id}")
    ArrayList<DishFlavor> getById(Long id);

}
