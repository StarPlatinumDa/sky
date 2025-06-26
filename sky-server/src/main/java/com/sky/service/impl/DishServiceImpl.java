package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishService dishService;


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

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page=dishMapper.page(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());

    }

    @Transactional// 事务保持原子性
    @Override
    public void deleteBatch(ArrayList<Long> ids) {
        // 判断当前菜品是否能够删除  是否已经在售？
        for (Long id : ids) {
            int status=dishMapper.statusById(id);
            if(status== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 判断当前菜品是否已经关联套餐
        ArrayList<Long> setmealIds=setmealDishMapper.getSetMealDishIdsByIds(ids);
        if(setmealIds!=null&& !setmealIds.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除dish表
//        for (Long id : list) {
//            dishMapper.deleteById(id);
//            // 删除时要连带删除口味表中的部分
//            dishFlavorMapper.deleteByDishId(id);
//        }
        // 不用for循环，而是一次性批量删除
        // delete from dish where id in (1,2,3) 动态sql
        dishMapper.deleteBatchId(ids);
        // 删除时要连带删除口味表中的部分
        dishFlavorMapper.deleteBatchId(ids);
    }

    @Transactional
    public DishVO getById(Long id) {
        DishVO dishVO = new DishVO();
        Dish dish=dishMapper.getById(id);
        BeanUtils.copyProperties(dish,dishVO);
        ArrayList<DishFlavor> dishFlavors=dishFlavorMapper.getById(id);
        if(dishFlavors!=null&& !dishFlavors.isEmpty()){
            dishVO.setFlavors(dishFlavors);
        }
        return dishVO;
    }

    @Transactional // 会修改两个表所以要事务
    public void updateDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        // 口味表先删除再新增，这样就不用考虑多种复杂情况了(部分修改部分新增)
        dishFlavorMapper.deleteByDishId(dish.getId());
        dishFlavorMapper.insertBatch(dishDTO.getFlavors());
    }
}
