package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.ArrayList;
import java.util.List;

public interface DishService {
    void saveWithFlavor(DishDTO dishDTO);

    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(ArrayList<Long> list);

    DishVO getById(Long id);

    void updateDish(DishDTO dishDTO);

    List<DishVO> listWithFlavor(Dish dish);

    ArrayList<Dish> getListById(Integer categoryId);

    void status(Integer status, Long id);
}
