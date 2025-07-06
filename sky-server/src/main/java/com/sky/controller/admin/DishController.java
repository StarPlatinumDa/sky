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
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    @Autowired// 用户端增删改,改Status时清理redis缓存
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
        // 清理缓存数据
        String key="dish_"+dishDTO.getCategoryId();
        redisTemplate.delete(key);
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

        // 批量删除仅有id，删除后可能多个属于同一类别，也可能多个，还需要查数据库，所以不如直接都删了
        // redis只有查询时才识别通配符所以先查再删
        cleanCache("dish_*");

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

        // 修改根据是否修改了分类，可能删一个或两个，但如果修改了分类，就还要数据库查询原来的分类
        // 所以由于修改操作不是常规操作，就全删了
        cleanCache("dish_*");
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

    /**
     * 菜品起售停售
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result status(@PathVariable Integer status,Long id){
        log.info("菜品起售停售,{}",status+","+id);
        dishService.status(status,id);
        // 只传入id，获取类别要查数据库，所以全删了
        cleanCache("dish_*");
        return Result.success();
    }

    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }


}
