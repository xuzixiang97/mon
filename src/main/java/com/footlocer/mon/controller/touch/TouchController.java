package com.footlocer.mon.controller.touch;

import com.footlocer.mon.entity.ShoeExcle;
import com.footlocer.mon.manager.TouchService;
import com.footlocer.mon.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/touch")
public class TouchController {

    @Autowired
    private TouchService touchService;


    @GetMapping("/test")
    public String hello() {
        return "aaa";
    }

    @GetMapping("/check")
    public void hello1(HttpServletResponse response) throws Exception {
        List<String> skuList = TxtUtil.readTxtFile("D:\\bot\\出口\\skuAll.txt");
        List<ShoeExcle> check = touchService.check(skuList);
        //输出 html
        ExcleUtil.exportTouch(check, response);

    }




}
