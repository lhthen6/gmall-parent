package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface TrademarkService {
    List<BaseTrademark> getTrademarkList();

    IPage<BaseTrademark> getBaseTrademark(Long pageNo, Long pageSize);
}
