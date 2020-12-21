package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class OrderController {

    @Autowired
    private OrderFeignClient orderFeignClient;
    @Autowired
    private UserFeignClient userFeignClient;

    @RequestMapping("trade.html")
    public String trade(HttpServletRequest request, Model model) {

        String userId = request.getHeader("userId");

        List<OrderDetail> orderDetails = orderFeignClient.trade(userId);
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);

        model.addAttribute("detailArrayList", orderDetails);
        model.addAttribute("userAddressList", userAddressList);
        model.addAttribute("totalAmount", getTotalAmount(orderDetails));
        String tradeNo = orderFeignClient.genTradeNo(userId);
        model.addAttribute("tradeNo", tradeNo);
        return "order/trade";

    }

    private BigDecimal getTotalAmount(List<OrderDetail> orderDetails) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OrderDetail orderDetail : orderDetails) {
            BigDecimal orderPrice = orderDetail.getOrderPrice();
            totalAmount = totalAmount.add(orderPrice);
        }
        return totalAmount;
    }

    @RequestMapping("myOrder.html")
    public String myOrder() {

        return "order/myOrder";

    }

}
