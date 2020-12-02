package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SpuImage;

import java.util.List;

public interface SpuImageService {
    List<SpuImage> getSpuImageList(Long spuId);
}
