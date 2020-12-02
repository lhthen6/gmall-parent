package com.atguigu.gmall.product.client;

import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@FeignClient("service-product")
public interface ProductFeignClient {

    @RequestMapping("api/product/getPrice/{skuId}")
    BigDecimal getPrice(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/product/getSkuInfoBySkuId/{skuId}")
    SkuInfo getSkuInfoBySkuId(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/product/getSpuSaleAttrBySpuId/{spuId}")
    List<SpuSaleAttr> getSpuSaleAttrBySpuId(@PathVariable("spuId") Long spuId);

    @RequestMapping("api/product/getBaseCategoryViewByCategory3Id/{category3Id}")
    BaseCategoryView getBaseCategoryViewByCategory3Id(@PathVariable("category3Id") Long category3Id);
}