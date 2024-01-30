package com.footlocer.mon.controller.touch;

import com.footlocer.mon.entity.ShoeExcle;
import com.footlocer.mon.entity.TouchSku;
import com.footlocer.mon.manager.TouchService;
import com.footlocer.mon.service.ITouchSkuService;
import com.footlocer.mon.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/touch")
public class TouchController {

    @Autowired
    private TouchService touchService;

    @Autowired
    private ITouchSkuService touchSkuService;


    @GetMapping("/test")
    public String hello() {
        return "aaa";
    }

    @GetMapping("/check")
    public void hello1(HttpServletResponse response) throws Exception {
        List<TouchSku> touchSkuList = touchSkuService.list();
        List<String> skuList = touchSkuList.stream().map(TouchSku::getSku).collect(Collectors.toList());
        List<ShoeExcle> check = touchService.check(skuList);
        //输出 html
        ExcleUtil.exportTouch(check, response);

    }




}
