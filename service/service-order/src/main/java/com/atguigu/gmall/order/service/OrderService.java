package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

public interface OrderService {

    String submitOrder(OrderInfo order);

    String genTradeNo(String userId);

    boolean checkTradeNo(String userId, String tradeNo);

    OrderInfo getOrderInfoById(Long orderId);
}
