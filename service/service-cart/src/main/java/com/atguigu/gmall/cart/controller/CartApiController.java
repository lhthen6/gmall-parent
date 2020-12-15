package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/cart")
@CrossOrigin
public class CartApiController {

    @Autowired
    private CartService cartService;

    @RequestMapping("cartList")
    Result cartList() {
        CartInfo cartInfo = new CartInfo();
        String userId = "1"; //TODO userId先写死，以后加入网关再改
        cartInfo.setUserId(userId);

        List<CartInfo> cartInfos = cartService.cartList(cartInfo);
        return Result.ok(cartInfos);
    }

    @RequestMapping("addCart")
    void addCart(@RequestBody CartInfo cartInfo) {

        String userId = "1"; //TODO 临时写死userId

        cartInfo.setUserId(userId);

        cartService.addCart(cartInfo);

    }

}
