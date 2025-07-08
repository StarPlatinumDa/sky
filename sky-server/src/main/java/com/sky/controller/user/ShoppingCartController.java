package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "用户端(C端)购物车模块")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    @ApiOperation("添加购物车")
//    @CachePut(cacheNames = "shoppingcartCache",key = "#shoppingCartDTO.")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车，商品信息为：{},",shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return null;
    }

    /**
     * 根据用户id返回所有购物车
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result list(){
        Long userId = BaseContext.getCurrentId();
        log.info("根据用户id返回所有购物车,用户id为{}",userId);
        ArrayList<ShoppingCart> cartList=shoppingCartService.list(userId);
        return Result.success(cartList);
    }
}
