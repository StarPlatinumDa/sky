package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired // 注入config中的bean
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        log.info("根据分类id查询菜品,{}",categoryId);

        // 构造对应的redis key:dish_categoryId
        String key="dish_"+categoryId;


        // 查询redis中是否存在
        ValueOperations operations = redisTemplate.opsForValue();
        // 放进去的是什么取出来就是什么(只是在redis底层是字符串，拿出来会自动转化)
        List<DishVO> list = (List<DishVO>) operations.get(key);
        if(list!=null && !list.isEmpty()){
            // 如果存在，直接返回，无需查询数据库
            return Result.success(list);
        }



        // 不存在，查询数据库
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        list = dishService.listWithFlavor(dish);
        // 将查询到的数据放入redis中
        operations.set(key,list);

        return Result.success(list);
    }

}
