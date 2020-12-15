package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.config.GmallCache;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.product.mapper.SkuImageMapper;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SkuInfoServiceImpl implements SkuInfoService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ListFeignClient listFeignClient;

    @Override
    public void skuInfoService(SkuInfo skuInfo) {
        skuInfoMapper.insert(skuInfo);
        Long skuId = skuInfo.getId();

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (null != skuImageList) {
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuId);
                skuImageMapper.insert(skuImage);
            }
        }

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (null != skuAttrValueList) {
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuId);
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (null != skuSaleAttrValueList) {
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuId);
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }
    }

    @Override
    public IPage<SkuInfo> getSkuList(Long pageNo, Long pageSize) {
        IPage<SkuInfo> page = new Page<>(pageNo, pageSize);
        IPage<SkuInfo> skuInfoIPage = skuInfoMapper.selectPage(page, null);
        return skuInfoIPage;
    }

    @Override
    public void onSale(Long skuId) {
        // mysql 下架
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setIsSale(1);
        skuInfo.setId(skuId);
        skuInfoMapper.updateById(skuInfo);

        // 清理nosql
        System.out.println("同步搜索引擎");
        listFeignClient.onSale(skuId);
    }

    @Override
    public void cancelSale(Long skuId) {
        // mysql 下架
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setIsSale(0);
        skuInfo.setId(skuId);
        skuInfoMapper.updateById(skuInfo);

        // 清理nosql
        System.out.println("同步搜索引擎");
        listFeignClient.cancelSale(skuId);
    }

    @Override
    public BigDecimal getPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        return skuInfo.getPrice();
    }

    @GmallCache
    @Override
    public SkuInfo getSkuInfoBySkuId(Long skuId) {
        SkuInfo skuInfo = getSkuInfoBySkuIdFromDb(skuId);
        return skuInfo;
    }

    private SkuInfo getSkuInfoBySkuIdBak(Long skuId) {
        SkuInfo skuInfo = null;
        // 访问noSql
        skuInfo = (SkuInfo) redisTemplate.opsForValue().get(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX);
        if (null == skuInfo) {
            String key = UUID.randomUUID().toString();
            Boolean OK = redisTemplate.opsForValue().setIfAbsent("sku:" + skuId + ":lock", key, 3, TimeUnit.SECONDS);
            if (OK) {
                //访问db
                skuInfo = getSkuInfoBySkuIdFromDb(skuId);
                if (null != skuInfo) {
                    //同步缓存
                    redisTemplate.opsForValue().set(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX, skuInfo);
                    // 释放锁方法一
                    String openKey = (String) redisTemplate.opsForValue().get("sku:" + skuId + ":lock");
                    if (key.equals(openKey)) {
                        redisTemplate.delete("sku:" + skuId + ":lock");
                    }
//                // 释放锁方法二
//                // 解锁：使用lua 脚本解锁
//                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//                // 设置lua脚本返回的数据类型
//                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
//                // 设置lua脚本返回类型为Long
//                redisScript.setResultType(Long.class);
//                redisScript.setScriptText(script);
//                // 删除key 所对应的 value
//                redisTemplate.execute(redisScript, Arrays.asList("sku:" + skuId + ":lock"), key);
                } else {
                    // 同步空缓存
                    redisTemplate.opsForValue().set(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX, skuInfo,5,TimeUnit.SECONDS);
                }
            } else {
                // 自旋
                return getSkuInfoBySkuId(skuId);
            }
        }
        return skuInfo;
    }

    private SkuInfo getSkuInfoBySkuIdFromDb(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        QueryWrapper<SkuImage> skuImageQueryWrapper = new QueryWrapper<>();
        skuImageQueryWrapper.eq("sku_id", skuId);
        List<SkuImage> skuImageList = skuImageMapper.selectList(skuImageQueryWrapper);
        skuInfo.setSkuImageList(skuImageList);
        return skuInfo;
    }
}
