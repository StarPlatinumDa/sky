package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface SetmealDishMapper {
    // select setmeal_id from setmeal_dish where id in (1,2,3)  最后的集合要用动态sql
    ArrayList<Long> getSetMealDishIdsByIds(ArrayList<Long> ids);

    /**
     * 批量插入
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 批量删除
     * @param ids
     */
    void deleteBatch(ArrayList<Long> ids);

    /**
     * 根据setmeal_id查询所有关联的setmealDish
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id=#{id}")
    ArrayList<SetmealDish> getDishsById(Long id);
}
