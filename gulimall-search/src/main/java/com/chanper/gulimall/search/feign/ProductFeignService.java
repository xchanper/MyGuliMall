package com.chanper.gulimall.search.feign;


import com.chanper.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <p>Title: ProductFeignService</p>
 * Description：
 * date：2020/6/22 23:25
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @RequestMapping("product/attr/info/{attrId}")
    R info(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/brand/infos")
    R brandInfo(@RequestParam("brandIds") List<Long> brandIds);
}
