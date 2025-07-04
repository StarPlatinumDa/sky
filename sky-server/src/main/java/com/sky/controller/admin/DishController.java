package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询,{}",dishPageQueryDTO);
        PageResult result=dishService.page(dishPageQueryDTO);
        return Result.success(result);
    }

    @DeleteMapping
    @ApiOperation("批量删除")
    // 自动处理 "1,2,3" 字符串的id
    public Result deleteBatch(@RequestParam ArrayList<Long> ids){
        log.info("批量删除,{}",ids);
        dishService.deleteBatch(ids);

        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品信息")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品信息");
        DishVO dishVO=dishService.getById(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateDish(@RequestBody DishDTO dishDTO){
        log.info("修改菜品，{}",dishDTO);
        dishService.updateDish(dishDTO);
        return Result.success();
    }

    /**
     * 根据菜品类别查菜品列表
     * @return
     */
    @GetMapping("list")
    @ApiOperation("根据菜品类别查菜品列表")
    public Result getListById(Integer categoryId){
        log.info("根据菜品类别查菜品列表,{}",categoryId);
        ArrayList<Dish> dishArrayList= dishService.getListById(categoryId);
        return Result.success(dishArrayList);
    }

}
