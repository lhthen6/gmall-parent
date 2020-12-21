package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("api/cart")
public class CartApiController {

    @Autowired
    private CartService cartService;

    @RequestMapping("cartList/{userId}")
    List<CartInfo> cartList(@PathVariable("userId") String userId) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfos = cartService.cartList(cartInfo);
        return cartInfos;
    }

    @RequestMapping("cartList")
    Result cartList(HttpServletRequest request) {
        CartInfo cartInfo = new CartInfo();
        String userId = request.getHeader("userId");
        cartInfo.setUserId(userId);

        List<CartInfo> cartInfos = cartService.cartList(cartInfo);
        return Result.ok(cartInfos);
    }

    @RequestMapping("addCart")
    void addCart(@RequestBody CartInfo cartInfo, HttpServletRequest request) {

        String userId = request.getHeader("userId");

        cartInfo.setUserId(userId);

        cartService.addCart(cartInfo);

    }

}
