package com.footlocer.mon.controller.touch;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.footlocer.mon.dto.FootlocerBaseInfo;
import com.footlocer.mon.dto.Item;
import com.footlocer.mon.dto.Offers;
import com.footlocer.mon.dto.StockInfo;
import com.footlocer.mon.dto.touch.ShoeExcle;
import com.footlocer.mon.service.TouchService;
import com.footlocer.mon.util.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
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
        List<String> skuList = TxtUtil.readTxtFile("D:\\bot\\出口\\sku1.txt");
        List<ShoeExcle> check = touchService.check(skuList);
        //输出 html
        ExcleUtil.exportTouch(check, response);

    }




}
