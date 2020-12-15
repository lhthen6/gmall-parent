package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void addCart(CartInfo cartInfo) {
        Long skuId = cartInfo.getSkuId();
        Integer skuNum = cartInfo.getSkuNum();
        String userId = cartInfo.getUserId();

        // 查询购物车里是否已经添加
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("sku_id", skuId);
        wrapper.eq("user_id", userId);
        CartInfo cartInfoFromDb = cartInfoMapper.selectOne(wrapper);

        if (null == cartInfoFromDb) {
            // 查询skuId对应的sku信息并放入cartInfo
            SkuInfo skuInfoBySkuId = productFeignClient.getSkuInfoBySkuId(skuId);// 查询sku详情
            cartInfo.setIsChecked(1);
            cartInfo.setUserId(userId);
            cartInfo.setSkuId(skuId);
            cartInfo.setSkuNum(skuNum);
            cartInfo.setImgUrl(skuInfoBySkuId.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfoBySkuId.getSkuName());
            cartInfo.setCartPrice(skuInfoBySkuId.getPrice().multiply(new BigDecimal(skuNum)));
            // 购物车保存时，没有skuPrice字段，因为一致性差，skuPrice字段只能从sku表中查询

            cartInfoMapper.insert(cartInfo);
        } else {
            cartInfo = cartInfoFromDb;// 如果已经添加过，要用db的cartInfo覆盖掉缓存
            cartInfo.setSkuNum(cartInfoFromDb.getSkuNum() + skuNum);
            cartInfoMapper.update(cartInfo, wrapper);
        }

        // 同步缓存
        redisTemplate.opsForHash().put(RedisConst.USER_KEY_PREFIX + cartInfo.getUserId() + RedisConst.USER_CART_KEY_SUFFIX, skuId + "", cartInfo);


    }

    @Override
    public List<CartInfo> cartList(CartInfo cartInfo) {
        // 先取缓存数据
        List<CartInfo> cartInfos = (List<CartInfo>) redisTemplate.opsForHash().values("user:" + cartInfo.getUserId() + ":cart");

        if (null == cartInfos && cartInfos.size() < 0) {
            HashMap<String, Object> cacheMap = new HashMap<>();
            // 查询数据库
            QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", cartInfo.getUserId());
            cartInfos = cartInfoMapper.selectList(wrapper);
            if (null != cartInfos && cartInfos.size() > 0) {
                for (CartInfo info : cartInfos) {
                    cacheMap.put(info.getSkuId() + "", info);
                }
                // 同步缓存
                redisTemplate.opsForHash().putAll("user:" + cartInfo.getUserId() + ":cart", cacheMap);
            }
        }

        // 只有在页面展示时放入skuPrice
        if (null != cartInfos && cartInfos.size() > 0) {
            for (CartInfo info : cartInfos) {
                SkuInfo skuInfoBySkuId = productFeignClient.getSkuInfoBySkuId(info.getSkuId());
                info.setSkuPrice(skuInfoBySkuId.getPrice());
            }
        }
        return cartInfos;
    }

}
