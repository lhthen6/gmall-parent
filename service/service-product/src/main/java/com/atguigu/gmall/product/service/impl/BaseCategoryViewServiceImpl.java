package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.service.BaseCategoryViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseCategoryViewServiceImpl implements BaseCategoryViewService {

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Override
    public BaseCategoryView getBaseCategoryViewByCategory3Id(Long category3Id) {
        BaseCategoryView baseCategoryView = baseCategoryViewMapper.selectById(category3Id);
        return baseCategoryView;
    }

}
