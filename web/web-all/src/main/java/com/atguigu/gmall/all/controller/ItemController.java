package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Autowired
    private ItemFeignClient itemFeignClient;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId, Model model) {
        Map<String, Object> map = new HashMap<>();
        map = itemFeignClient.getItem(skuId);
        model.addAllAttributes(map);
        return "item/index";
    }

    @RequestMapping("/")
    public String index(){
        return "index";
    }

    @RequestMapping("test")
    public String test(Model model){

        model.addAttribute("hello","hello thymeleaf");

        List<String> list = new ArrayList<>();

        for (int i = 0; i < 5 ; i++) {
            list.add("元素"+i);
        }


        model.addAttribute("list",list);


        model.addAttribute("num","亡者联盟");
        return "test";
    }

}
