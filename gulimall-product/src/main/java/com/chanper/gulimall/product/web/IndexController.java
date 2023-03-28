package com.chanper.gulimall.product.web;

import com.chanper.gulimall.product.entity.CategoryEntity;
import com.chanper.gulimall.product.service.CategoryService;
import com.chanper.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @RequestMapping({"/", "index", "/index.html"})
    public String indexPage(Model model) {
        // 获取一级分类所有缓存
        List<CategoryEntity> catagories = categoryService.getLevel1Categorys();
        model.addAttribute("catagories", catagories);
        return "index";
    }

    @ResponseBody
    @RequestMapping("index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatlogJson() {

        Map<String, List<Catelog2Vo>> map = categoryService.getCatelogJson();
        return map;
    }

}
