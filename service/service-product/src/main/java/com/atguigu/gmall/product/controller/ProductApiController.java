package com.atguigu.gmall.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.BaseCategoryViewService;
import com.atguigu.gmall.product.service.CategoryService;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/product")
public class ProductApiController {

    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SpuSaleAttrService spuSaleAttrService;
    @Autowired
    private BaseCategoryViewService baseCategoryViewService;
    @Autowired
    private CategoryService categoryService;

    @RequestMapping("getBaseCategoryList")
    List<JSONObject> getBaseCategoryList() {
        List<JSONObject> list = categoryService.getBaseCategoryList();
        return list;
    }

    @RequestMapping("getSaleAttrValuesBySpuId/{spuId}")
    Map<String, Long> getSaleAttrValuesBySpuId(@PathVariable Long spuId) {
        Map<String, Long> map = spuSaleAttrService.getSaleAttrValuesBySpuId(spuId);
        return map;
    }

    @RequestMapping("getPrice/{skuId}")
    public BigDecimal getPrice(@PathVariable Long skuId) {
        BigDecimal bigDecimal = new BigDecimal(0);
        bigDecimal = skuInfoService.getPrice(skuId);
        return bigDecimal;
    }

    @RequestMapping("getSkuInfoBySkuId/{skuId}")
    public SkuInfo getSkuInfoBySkuId(@PathVariable("skuId") Long skuId) {
        SkuInfo skuInfo = skuInfoService.getSkuInfoBySkuId(skuId);
        return skuInfo;
    }

    @RequestMapping("getSpuSaleAttrBySpuId/{spuId}/{skuId}")
    public List<SpuSaleAttr> getSpuSaleAttrBySpuId(@PathVariable("spuId") Long spuId, @PathVariable("skuId") Long skuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrService.getSpuSaleAttrListCheckBySku(spuId, skuId);
        return spuSaleAttrList;
    }

    @RequestMapping("getBaseCategoryViewByCategory3Id/{category3Id}")
    public BaseCategoryView getBaseCategoryViewByCategory3Id(@PathVariable("category3Id") Long category3Id) {
        BaseCategoryView baseCategoryView = baseCategoryViewService.getBaseCategoryViewByCategory3Id(category3Id);
        return baseCategoryView;
    }

}
