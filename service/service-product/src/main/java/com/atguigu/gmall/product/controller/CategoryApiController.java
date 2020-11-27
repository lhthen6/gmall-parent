package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/product")
@CrossOrigin
public class CategoryApiController {

    @RequestMapping("getCategory1")
    public Result getCategory1() {
        return Result.ok();
    }

}
