package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        // 判断是否已有菜品或套餐  查询时通过user_id作为约束查当前用户已有的商品  再加上这次的菜品id或(套餐id+口味)
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        ArrayList<ShoppingCart> list=shoppingCartMapper.list(shoppingCart);
            // 已有，说明是加一，即update操作
        if(list!=null&&!list.isEmpty()){
            // 如果查到只可能是一条数据
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber()+1);
            shoppingCartMapper.updateNumberById(cart);
        }else {
            // 没有，需要插入一条购物车数据
            // 要根据当前的套餐或菜品先获取到名称，图片和价格
            Long dishId = shoppingCartDTO.getDishId();
            if(dishId!=null){
                //本次新增的是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else {
                //本次新增的是套餐
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());

            shoppingCartMapper.insert(shoppingCart);


        }



    }

    /**
     * 根据用户id返回所有购物车
     * @param userId
     * @return
     */
    public ArrayList<ShoppingCart> list(Long userId) {
        ShoppingCart userCart = ShoppingCart.builder().userId(userId).build();
        ArrayList<ShoppingCart> list = shoppingCartMapper.list(userCart);
        return list;
    }

    @Override
    public void clean(Long userId) {
        shoppingCartMapper.deleteByUserId(userId);
    }

    /**
     * 删除购物车中的一条数据
     * @param shoppingCartDTO
     */
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        // 如果该条购物车中的数量为1，就把数量减一，否则直接删除
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());

        // 多种约束同时查询只会查出一条
        ArrayList<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list!=null&&!list.isEmpty()){
            // 重新赋值为查出的完整版
            shoppingCart=list.get(0);
            if(shoppingCart.getNumber()>1){
                // 数量大于1就减少
                shoppingCart.setNumber(shoppingCart.getNumber()-1);
                shoppingCartMapper.updateNumberById(shoppingCart);
            }else {
                // 数量为1则直接删除
                shoppingCartMapper.deleteBycartId(shoppingCart.getId());
            }
        }
    }
}
