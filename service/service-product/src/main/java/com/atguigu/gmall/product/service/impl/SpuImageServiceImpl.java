package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.product.mapper.SpuImageMapper;
import com.atguigu.gmall.product.service.SpuImageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpuImageServiceImpl implements SpuImageService {

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        QueryWrapper<SpuImage> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id", spuId);
        List<SpuImage> spuImages = spuImageMapper.selectList(wrapper);
        return spuImages;
    }
}
