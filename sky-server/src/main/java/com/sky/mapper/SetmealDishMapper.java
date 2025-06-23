package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

@Mapper
public interface SetmealDishMapper {
    // select setmeal_id from setmeal_dish where id in (1,2,3)  最后的集合要用动态sql
    ArrayList<Long> getSetMealDishIdsByIds(ArrayList<Long> list);
}
