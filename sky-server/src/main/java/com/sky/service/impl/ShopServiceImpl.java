package com.sky.service.impl;

import com.sky.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl implements ShopService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void setStatus(Integer status) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("SHOP_STATUS",status);
    }

    @Override
    public Integer getStatus() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 怎么放的就怎么接收
        Integer status = (Integer) valueOperations.get("SHOP_STATUS");
        return status;
    }
}
