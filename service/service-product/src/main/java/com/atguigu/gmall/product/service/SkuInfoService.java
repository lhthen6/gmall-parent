package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.math.BigDecimal;

public interface SkuInfoService {
    void skuInfoService(SkuInfo skuInfo);

    IPage<SkuInfo> getSkuList(Long pageNo, Long pageSize);

    void onSale(Long skuId);

    void cancelSale(Long skuId);

    BigDecimal getPrice(Long skuId);

    SkuInfo getSkuInfoBySkuId(Long skuId);
}
