package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.ArrayList;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 动态条件 userId+菜品id(或套餐id)+口味
     * 如果是userId+菜品Id就只能查到一条，如果是userId一个参数就是查用户的所有购物车数据
     * @param shoppingCart
     * @return
     */
    ArrayList<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据id修改数量
     * @param cart
     */
    @Update("update shopping_cart set number=#{number} where id=#{id}")
    void updateNumberById(ShoppingCart cart);

    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "values (#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据用户id清空购物车
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id=#{userId}")
    void deleteByUserId(Long userId);

    @Delete("delete from shopping_cart where id=#{id}")
    void deleteBycartId(Long id);
}
