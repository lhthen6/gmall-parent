package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public String submitOrder(OrderInfo order) {

        // 设置订单保存数据
        order.setOrderStatus(OrderStatus.UNPAID.toString());
        order.setProcessStatus(ProcessStatus.UNPAID.toString());
        // 设置日期
        Date date = new Date();
        Calendar instance = Calendar.getInstance();
        instance.add(1, Calendar.DATE);
        order.setExpireTime(instance.getTime());// 订单过期时间是当前时间+1
        order.setCreateTime(date);
        // 外部订单号
        String outTradeNo = "atguigu";
        outTradeNo += System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        outTradeNo += sdf.format(date);
        order.setOutTradeNo(outTradeNo);
        order.setOrderComment("fuck");
        List<OrderDetail> orderDetailList = order.getOrderDetailList();
        order.setTotalAmount(getTotalAmount(orderDetailList));
        orderInfoMapper.insert(order);

        Long orderId = order.getId();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderId);
            orderDetailMapper.insert(orderDetail);
        }
        return orderId + "";
    }

    @Override
    public String genTradeNo(String userId) {
        String tradeNo = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("user:" + userId + ":tradeCode", tradeNo);
        return tradeNo;
    }

    @Override
    public boolean checkTradeNo(String userId, String tradeNo) {
        boolean flag = false;
        String tradeNoFromCache = (String) redisTemplate.opsForValue().get("user:" + userId + ":tradeCode");
        if (!StringUtils.isEmpty(tradeNoFromCache) && tradeNoFromCache.equals(tradeNo)) {
            redisTemplate.delete("user:" + userId + ":tradeCode");
            flag = true;
        }
        return flag;
    }

    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        QueryWrapper<OrderDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(wrapper);
        orderInfo.setOrderDetailList(orderDetails);
        return orderInfo;
    }

    private BigDecimal getTotalAmount(List<OrderDetail> orderDetailList) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OrderDetail orderDetail : orderDetailList) {
            BigDecimal orderPrice = orderDetail.getOrderPrice();
            totalAmount = totalAmount.add(orderPrice);
        }
        return totalAmount;
    }
}
