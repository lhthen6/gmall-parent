package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/product")
@CrossOrigin
public class SkuApiController {

    @Autowired
    private SkuInfoService skuInfoService;

    @RequestMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        skuInfoService.skuInfoService(skuInfo);
        return Result.ok();
    }

    @RequestMapping("list/{pageNo}/{pageSize}")
    public Result getSkuList(@PathVariable Long pageNo, @PathVariable Long pageSize) {
        IPage<SkuInfo> skuInfoIPage = skuInfoService.getSkuList(pageNo, pageSize);
        return Result.ok(skuInfoIPage);
    }

    @RequestMapping("onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId) {
        skuInfoService.onSale(skuId);
        return Result.ok();
    }

    @RequestMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId) {
        skuInfoService.cancelSale(skuId);
        return Result.ok();
    }

}
