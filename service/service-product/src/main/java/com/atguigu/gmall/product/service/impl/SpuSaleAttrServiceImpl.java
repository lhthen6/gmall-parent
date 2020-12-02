package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.product.SpuSaleAttrValue;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrValueMapper;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpuSaleAttrServiceImpl implements SpuSaleAttrService {

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        QueryWrapper<SpuSaleAttr> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id", spuId);
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.selectList(wrapper);
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrs) {
            QueryWrapper<SpuSaleAttrValue> spuSaleAttrValueQueryWrapper = new QueryWrapper<>();
            spuSaleAttrValueQueryWrapper.eq("base_sale_attr_id", spuSaleAttr.getBaseSaleAttrId());
            spuSaleAttrValueQueryWrapper.eq("spu_id", spuSaleAttr.getSpuId());
            List<SpuSaleAttrValue> spuSaleAttrValues = spuSaleAttrValueMapper.selectList(spuSaleAttrValueQueryWrapper);
            spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValues);
        }
        return spuSaleAttrs;
    }
}
