package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.TrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("admin/product")
@CrossOrigin
public class TrademarkApiController {

    @Autowired
    private TrademarkService trademarkService;

    @RequestMapping("baseTrademark/getTrademarkList")
    public Result getTrademarkList() {
        List<BaseTrademark> trademarkList = trademarkService.getTrademarkList();
        return Result.ok(trademarkList);
    }

    @RequestMapping("baseTrademark/{pageNo}/{pageSize}")
    public Result getBaseTrademark(@PathVariable Long pageNo, @PathVariable Long pageSize) {
        IPage<BaseTrademark> baseTrademarkIPage = trademarkService.getBaseTrademark(pageNo, pageSize);
        return Result.ok(baseTrademarkIPage);
    }

}
