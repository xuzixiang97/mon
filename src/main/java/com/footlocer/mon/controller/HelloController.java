package com.footlocer.mon.controller;

import com.footlocer.mon.util.AnalysisUtil;
import com.footlocer.mon.util.XuHttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.net.www.http.HttpClient;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        AnalysisUtil.parseFlusProductDetail(html);
        return "aaa";
    }


}
