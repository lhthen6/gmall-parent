package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SpuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;


public interface SpuService {
    IPage<SpuInfo> getSpuList(Long pageNo, Long pageSize, Long category3Id);

    void saveSpuInfo(SpuInfo spuInfo);
}
