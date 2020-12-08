package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SpuSaleAttr;

import java.util.List;
import java.util.Map;

public interface SpuSaleAttrService {
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long spuId, Long skuId);

    Map<String, Long> getSaleAttrValuesBySpuId(Long spuId);
}
