package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public UserInfo verify(String token) {
        UserInfo userInfo = (UserInfo) redisTemplate.opsForValue().get("user:login:" + token);

        return userInfo;
    }

    @Override
    public UserInfo login(UserInfo userInfo) {

        String loginName = userInfo.getLoginName();
        String passwd = MD5.encrypt(userInfo.getPasswd());
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("login_name", loginName);
        wrapper.eq("passwd", passwd);
        userInfo = userInfoMapper.selectOne(wrapper);

        if (null == userInfo) {
            return null;
        } else {
            String token = UUID.randomUUID().toString();
            userInfo.setToken(token);
            redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + token, userInfo);
        }

        return userInfo;
    }

    @Override
    public List<UserAddress> findUserAddressListByUserId(String userId) {
        QueryWrapper<UserAddress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserAddress> userAddresses = userAddressMapper.selectList(wrapper);
        return userAddresses;
    }
}
