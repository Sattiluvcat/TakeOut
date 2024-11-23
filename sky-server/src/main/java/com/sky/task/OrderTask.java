package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;


    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOutOrder(){
        log.info("定时处理超时订单");
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-15);
        List<Orders> orders = orderMapper.getByStatusOrderTime(Orders.PENDING_PAYMENT, localDateTime);
        if(!orders.isEmpty()){
            for (Orders order : orders) {
            order.setStatus(Orders.CANCELLED);
            order.setCancelReason("已超时 自动取消");
            order.setCancelTime(LocalDateTime.now());
            orderMapper.update(order);
            }
        }

    }
    // 派送中订单设置
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDelivery(){
        log.info("处理超时订单");
        List<Orders> orders = orderMapper.getByStatusOrderTime(Orders.DELIVERY_IN_PROGRESS,
                LocalDateTime.now().plusMinutes(-60));
        if(!orders.isEmpty()){
            for (Orders order : orders) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}
