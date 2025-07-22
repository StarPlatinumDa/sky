package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，定时处理订单状态
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时未支付订单的方法
     */
    @Scheduled(cron = "0 * * * * *")// 每分钟触发一次
//    @Scheduled(cron = "0/5 * * * * *")
    public void processTimeoutOrder(){
        log.info("定时处理超时未支付订单,{}", LocalDateTime.now());
        Integer status=Orders.PENDING_PAYMENT;
        LocalDateTime orderTime = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList=orderMapper.getByStatusAndOrderTimeLess(status,orderTime);

        if(ordersList!=null&&!ordersList.isEmpty()){
            for (Orders orders : ordersList) {
                // 状态变为已取消
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());

                // 更新数据库
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理状态为派送中但已送达订单
     */
    @Scheduled(cron = "0 0 1 * * *")// 每天1点
    public void processDeliveredOrder(){
        log.info("定时处理派送中实际已送达的订单,{}", LocalDateTime.now());

        List<Orders> ordersList=orderMapper.getByStatusAndOrderTimeLess(
                Orders.DELIVERY_IN_PROGRESS,LocalDateTime.now().plusMinutes(-60));

        if(ordersList!=null&&!ordersList.isEmpty()){
            for (Orders orders : ordersList) {
                // 状态变为已完成
                orders.setStatus(Orders.COMPLETED);

                // 更新数据库
                orderMapper.update(orders);
            }
        }
    }
}
