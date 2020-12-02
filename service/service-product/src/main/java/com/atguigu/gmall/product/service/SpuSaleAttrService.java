package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SpuSaleAttr;

import java.util.List;

public interface SpuSaleAttrService {
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);
}
