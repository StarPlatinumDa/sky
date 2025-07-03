package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class UserShopController {
    @Autowired
    ShopService shopService;


    @GetMapping("/status")
    @ApiOperation("查询店铺状态")
    public Result getStatus(){
        Integer status=shopService.getStatus();
        log.info("1为营业中，查询店铺状态为,{}",status);
        return Result.success(status);
    }
}
