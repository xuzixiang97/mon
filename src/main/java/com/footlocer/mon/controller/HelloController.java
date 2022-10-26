package com.footlocer.mon.controller;

import com.footlocer.mon.dto.FootlocerBaseInfo;
import com.footlocer.mon.util.AnalysisUtil;
import com.footlocer.mon.util.XuHttpUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/hellow")
public class HelloController {

    @GetMapping("/test")
    public String hello() {
        return "aaa";
    }

    @GetMapping("/test1")
    public String hello1() throws IOException {

        String html = XuHttpUtil.getHtml("https://www.footlocker.com/en/product/~/J5423500.html");
        FootlocerBaseInfo footlocerBaseInfo = AnalysisUtil.parseFlusProductBaseDetail(html);
        return "aaa";
    }


}
