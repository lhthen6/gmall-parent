package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("admin/product")
@CrossOrigin
public class SpuApiController {

    @Autowired
    private SpuService spuService;
    @Autowired
    private BaseSaleAttrService baseSaleAttrService;
    @Autowired
    private SpuSaleAttrService spuSaleAttrService;
    @Autowired
    private SpuImageService spuImageService;

    @RequestMapping("{pageNo}/{pageSize}")
    public Result getSpuList(@PathVariable Long pageNo, @PathVariable Long pageSize, Long category3Id) {
        IPage<SpuInfo> spuInfoIPage = spuService.getSpuList(pageNo, pageSize, category3Id);
        return Result.ok(spuInfoIPage);
    }

    @RequestMapping("baseSaleAttrList")
    public Result getBaseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrService.getBaseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }

    @RequestMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        spuService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    @RequestMapping("spuSaleAttrList/{spuId}")
    public Result getSpuSaleAttrList(@PathVariable Long spuId) {
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrService.getSpuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrs);
    }

    @RequestMapping("spuImageList/{spuId}")
    public Result getSpuImageList(@PathVariable Long spuId) {
        List<SpuImage> spuImages = spuImageService.getSpuImageList(spuId);
        return Result.ok(spuImages);
    }

}
