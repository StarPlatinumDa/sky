package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/admin/category")
@Slf4j // 不用每次多写log的成员对象
@Api(tags = "分类相关接口")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result addNew(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类，参数为：{}",categoryDTO);
        categoryService.addNew(categoryDTO);

        return Result.success();
    }

    /**
     * 分类的分页查询
     */
    @GetMapping("/page")
    @ApiOperation("分类的分页查询")
    public Result page(CategoryPageQueryDTO categoryPageQueryDTO){
        PageResult pageResult=categoryService.page(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 分类删除
     * @return
     */
    @DeleteMapping()
    @ApiOperation("分类删除")
    public Result deleteById(Long id){
        log.info("删除分类:{}",id);
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 分类修改
     */
    @PutMapping
    @ApiOperation("分类修改")
    public Result update(@RequestBody CategoryDTO categoryDTO){
        log.info("分类修改,参数：{}",categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * 分类状态禁用
     */
    @PostMapping("/status/{status}")
    @ApiOperation("分类状态禁用")
    public Result status(@PathVariable Integer status,Long id){
        log.info("分类状态禁用,参数：{},{}",status,id);
        categoryService.status(status,id);
        return Result.success();
    }

    /**
     * 根据类型（菜品或者套餐 用于下拉框）查询分类
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result selectByType(Integer type){
        // 查询出的是状态为1，启用的，同时type是可选参数
        log.info("根据类型查询分类,{}",type);
        ArrayList<Category> categories=categoryService.selectByType(type);
        return Result.success(categories);
    }
}
