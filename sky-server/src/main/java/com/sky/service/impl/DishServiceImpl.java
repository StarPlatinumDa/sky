package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;



    // 多表需要一致性，所以要加事务注解  同时先获取第一个表的自增id，赋值给第二个表
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // 1.向菜品表插入一条数据  2.向口味表插入n条(可为0)数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        // mybatis useGeneratedKeys=ture得到插入后自增的id
        dishMapper.insert(dish);

        Long dishId=dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        // 如果有flavor就要存两张表
        if (flavors!=null && !flavors.isEmpty()){
            // 先遍历给每条赋值id
            flavors.forEach(it->it.setDishId(dishId));
            // 批量插入
            dishFlavorMapper.insertBatch(flavors);
        }


    }
}
