package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/order")
public class OrderApiController {

    @Autowired
    private CartFeignClient cartFeignClient;
    @Autowired
    private OrderService orderService;

    @RequestMapping("getOrderInfoById/{orderId}")
    OrderInfo getOrderInfoById(@PathVariable("orderId") Long orderId) {
        OrderInfo orderInfo = orderService.getOrderInfoById(orderId);
        return orderInfo;
    }

    @RequestMapping("genTradeNo/{userId}")
    public String genTradeNo(@PathVariable("userId") String userId) {
        String tradeNo = orderService.genTradeNo(userId);
        return tradeNo;
    }

    @RequestMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo order, HttpServletRequest request, String tradeNo) {
        String userId = request.getHeader("userId");

        // 验证tradeNo
        boolean flag = orderService.checkTradeNo(userId, tradeNo);
        if (flag) {
            order.setUserId(Long.parseLong(userId));
            String orderId = orderService.submitOrder(order);
            return Result.ok(orderId);
        } else {
            return Result.fail();
        }
    }

    @RequestMapping("trade/{userId}")
    public List<OrderDetail> trade(@PathVariable("userId") String userId) {

        List<CartInfo> cartInfos = cartFeignClient.cartList(userId);
        List<OrderDetail> orderDetails = new ArrayList<>();
        if (null != cartInfos && cartInfos.size() > 0) {
            for (CartInfo cartInfo : cartInfos) {
                if (cartInfo.getIsChecked() == 1) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setSkuNum(cartInfo.getSkuNum());
                    orderDetail.setSkuId(cartInfo.getSkuId());
                    orderDetail.setSkuName(cartInfo.getSkuName());
                    orderDetail.setImgUrl(cartInfo.getImgUrl());
                    BigDecimal skuNum = new BigDecimal(cartInfo.getSkuNum());
                    orderDetail.setOrderPrice(cartInfo.getCartPrice().multiply(skuNum));
                    orderDetails.add(orderDetail);
                }
            }
        }

        return orderDetails;
    }

}
