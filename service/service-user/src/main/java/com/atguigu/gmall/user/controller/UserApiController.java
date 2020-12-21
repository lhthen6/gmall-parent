package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/user/passport")
public class UserApiController {

    @Autowired
    private UserService userService;

    @RequestMapping("findUserAddressListByUserId/{userId}")
    List<UserAddress> findUserAddressListByUserId(@PathVariable("userId") String userId) {
        List<UserAddress> userAddresses = userService.findUserAddressListByUserId(userId);
        return userAddresses;
    }

    @RequestMapping("login")
    Result login(@RequestBody UserInfo userInfo, HttpServletRequest request) {

        userInfo = userService.login(userInfo);

        if (null != userInfo) {
            return Result.ok(userInfo);
        } else {
            return Result.fail();
        }
    }

    @RequestMapping("verify/{token}")
    Map<String, Object> verify(@PathVariable String token) {

        UserInfo userInfo = userService.verify(token);
        Map<String, Object> map = new HashMap<>();
        map.put("user", userInfo);

        return map;
    }

}
