package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public Map<String, Object> getItem(Long skuId) {
        return getMapThreadPool(skuId);
    }

    // Thread + ThreadPool 多线程加线程池
    private Map<String, Object> getMapThreadPool(Long skuId) {
        long start = System.currentTimeMillis();
        Map<String, Object> mapResult = new HashMap<>();

        CompletableFuture<Void> completableFuturePrice = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                BigDecimal bigDecimal = productFeignClient.getPrice(skuId);
                mapResult.put("price", bigDecimal);
            }
        }, threadPoolExecutor);

        CompletableFuture<SkuInfo> completableFutureSkuInfo = CompletableFuture.supplyAsync(new Supplier<SkuInfo>() {
            @Override
            public SkuInfo get() {
                SkuInfo skuInfo = productFeignClient.getSkuInfoBySkuId(skuId);
                mapResult.put("skuInfo", skuInfo);
                return skuInfo;
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> completableFutureSaleAttrs = completableFutureSkuInfo.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrBySpuId(skuInfo.getSpuId(), skuId);
                mapResult.put("spuSaleAttrList", spuSaleAttrList);
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> completableFutureCategory = completableFutureSkuInfo.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                BaseCategoryView baseCategoryView = productFeignClient.getBaseCategoryViewByCategory3Id(skuInfo.getCategory3Id());
                mapResult.put("categoryView", baseCategoryView);
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> completableFutureSkuJsonMap = completableFutureSkuInfo.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                //根据spuId查询出来的sku和销售属性值id的对应关系hash表
                Map<String, Long> map = productFeignClient.getSaleAttrValuesBySpuId(skuInfo.getSpuId());
                mapResult.put("valuesSkuJson", JSON.toJSONString(map));
            }
        }, threadPoolExecutor);

        CompletableFuture.allOf(completableFutureSkuInfo, completableFuturePrice, completableFutureSaleAttrs, completableFutureCategory,completableFutureSkuJsonMap).join();

        long end = System.currentTimeMillis();
        System.out.println("运行时间：" + (end - start));

        return mapResult;
    }

    // Thread 多线程
    private Map<String, Object> getMapThread(Long skuId) {
        long start = System.currentTimeMillis();
        Map<String, Object> mapResult = new HashMap<>();

        CompletableFuture<Void> completableFuturePrice = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                BigDecimal bigDecimal = productFeignClient.getPrice(skuId);
                mapResult.put("price", bigDecimal);
            }
        });

        CompletableFuture<SkuInfo> completableFutureSkuInfo = CompletableFuture.supplyAsync(new Supplier<SkuInfo>() {
            @Override
            public SkuInfo get() {
                SkuInfo skuInfo = productFeignClient.getSkuInfoBySkuId(skuId);
                mapResult.put("skuInfo", skuInfo);
                return skuInfo;
            }
        });

        CompletableFuture<Void> completableFutureSaleAttrs = completableFutureSkuInfo.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrBySpuId(skuInfo.getSpuId(), skuId);
                mapResult.put("spuSaleAttrList", spuSaleAttrList);
            }
        });

        CompletableFuture<Void> completableFutureCategory = completableFutureSkuInfo.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                BaseCategoryView baseCategoryView = productFeignClient.getBaseCategoryViewByCategory3Id(skuInfo.getCategory3Id());
                mapResult.put("categoryView", baseCategoryView);
            }
        });

        CompletableFuture<Void> completableFutureSkuJsonMap = completableFutureSkuInfo.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                //根据spuId查询出来的sku和销售属性值id的对应关系hash表
                Map<String, Long> map = productFeignClient.getSaleAttrValuesBySpuId(skuInfo.getSpuId());
                mapResult.put("valuesSkuJson", JSON.toJSONString(map));
            }
        });

        CompletableFuture.allOf(completableFutureSkuInfo, completableFuturePrice, completableFutureSaleAttrs, completableFutureCategory,completableFutureSkuJsonMap).join();

        long end = System.currentTimeMillis();
        System.out.println("运行时间：" + (end - start));

        return mapResult;
    }

    // single 单线程
    private Map<String, Object> getMapSingle(Long skuId) {
        long start = System.currentTimeMillis();

        Map<String, Object> mapResult = new HashMap<>();
        BigDecimal bigDecimal = productFeignClient.getPrice(skuId);
        SkuInfo skuInfo = productFeignClient.getSkuInfoBySkuId(skuId);
        List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrBySpuId(skuInfo.getSpuId(), skuId);
        BaseCategoryView baseCategoryView = productFeignClient.getBaseCategoryViewByCategory3Id(skuInfo.getCategory3Id());
        //根据spuId查询出来的sku和销售属性值id的对应关系hash表
        Map<String, Long> map = productFeignClient.getSaleAttrValuesBySpuId(skuInfo.getSpuId());
        mapResult.put("price", bigDecimal);
        mapResult.put("skuInfo", skuInfo);
        mapResult.put("spuSaleAttrList", spuSaleAttrList);
        mapResult.put("categoryView", baseCategoryView);
        mapResult.put("valuesSkuJson", JSON.toJSONString(map));

        long end = System.currentTimeMillis();
        System.out.println("运行时间：" + (end - start));
        return mapResult;
    }
}
