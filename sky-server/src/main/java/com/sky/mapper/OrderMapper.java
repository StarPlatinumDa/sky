package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 根据订单状态和下单时间查询超时订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status=#{status} and order_time<#{orderTime}")
    List<Orders> getByStatusAndOrderTimeLess(Integer status, LocalDateTime orderTime);

    /**
     * 根据id查订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 获取历史订单，分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO);


    @Select("select sum(amount) from orders where order_time>=#{from} and order_time<#{to} and orders.status=#{status}")
    Double getSumByDate(LocalDate from, LocalDate to, Integer status);
}
