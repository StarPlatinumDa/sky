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
        // 不能return null,如果return null前端取不到正确的返回状态(promise包含code msg等)就不能正常通过回调函数更新购物车
        return Result.success();
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

    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result clean(){
        Long userId = BaseContext.getCurrentId();
        log.info("根据用户id清空购物车,用户id为{}",userId);
        shoppingCartService.clean(userId);
        return Result.success();
    }

    /**
     * 删除购物车中的一条数据
     * @param shoppingCartDTO
     * @return
     */
    // 这里不能用delete和put，因为delete不推荐设置body(无法解析对象，一般都是path传id)
    // 同时put需要是幂等的修改，即执行多次，都是一样的结果，每次减1显然不会是相等的
    @PostMapping("/sub")
    @ApiOperation("删除购物车的一条数据")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("删除购物车的一条数据,{}",shoppingCartDTO);
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success();
    }

}
