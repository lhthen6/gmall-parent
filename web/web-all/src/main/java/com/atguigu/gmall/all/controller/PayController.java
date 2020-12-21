package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PayController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @RequestMapping("paySuccess.html")
    public String paySuccess() {

        return "payment/success";
    }

    @RequestMapping("pay.html")
    public String pay(Long orderId, Model model) {

        OrderInfo orderInfo = orderFeignClient.getOrderInfoById(orderId);

        model.addAttribute("orderInfo", orderInfo);

        return "payment/pay";

    }

}
