package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    void insert(Orders orders);

    @Select("select * from orders where number=#{orderNumber}")
    Long getOrderId(String orderNumber);

    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} where id = #{id}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus,LocalDateTime check_out_time, Long id);

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

    Page<Orders> query(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    @Select("select count(*) from orders where status=#{status}")
    int countByStatus(Integer status);

    @Select("select * from orders where status=#{status} and order_time<#{orderTime}")
    List<Orders> getByStatusOrderTime(Integer status,LocalDateTime orderTime);

    Double getStaticByDate(HashMap map);

    Integer getOrder(Integer o, LocalDateTime firstTime, LocalDateTime endTime);


    @Select("select od.name,sum(od.number) number from order_detail od ,orders o " +
            "where order_id=o.id and o.status=5 " +
            "and o.order_time>=#{beginTime} and o.order_time<=#{endTime} " +
            "group by od.name order by number desc limit 0,10;")
    List<GoodsSalesDTO> getTop10(LocalDateTime beginTime, LocalDateTime endTime);

    Integer countByMap(Map map);
}
