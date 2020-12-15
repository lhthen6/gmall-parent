package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CartController {

    @Autowired
    private CartFeignClient cartFeignClient;

    @RequestMapping("addCart.html")
    public String addCart(CartInfo cartInfo, Long skuId, Long skuNum) {

        cartFeignClient.addCart(cartInfo);

        return "redirect:/cart/addCart.html?skuNum=" + cartInfo.getSkuNum();
    }

    @RequestMapping("cart/cart.html")
    public String cartList() {

        System.out.println("fuck");

        return "cart/index";
    }

}
