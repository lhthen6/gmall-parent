package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.TrademarkMapper;
import com.atguigu.gmall.product.service.TrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrademarkServiceImpl implements TrademarkService {

    @Autowired
    private TrademarkMapper trademarkMapper;

    @Override
    public List<BaseTrademark> getTrademarkList() {
        List<BaseTrademark> trademarkList = trademarkMapper.selectList(null);
        return trademarkList;
    }

    @Override
    public IPage<BaseTrademark> getBaseTrademark(Long pageNo, Long pageSize) {
        IPage<BaseTrademark> baseTrademarkPage = new Page<>(pageNo, pageSize);
        IPage<BaseTrademark> baseTrademarkIPage = trademarkMapper.selectPage(baseTrademarkPage, null);
        return baseTrademarkIPage;
    }
}
