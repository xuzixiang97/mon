package com.footlocer.mon.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.footlocer.mon.dto.FootlocerBaseInfo;
import com.footlocer.mon.dto.StockInfo;
import com.footlocer.mon.util.AnalysisUtil;
import com.footlocer.mon.util.XuHttpUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/hellow")
public class HelloController {

    @GetMapping("/test")
    public String hello() {
        return "aaa";
    }

    @GetMapping("/test1")
    public void hello1(HttpServletResponse response) throws IOException {

        List<StockInfo> list = new ArrayList<>();


        // todo 读取列表
        // todo 换代理
        String html = XuHttpUtil.getHtml("https://www.footlocker.com/en/product/~/J5423500.html");
        FootlocerBaseInfo footlocerBaseInfo = AnalysisUtil.parseFlusProductBaseDetail(html);



        list.add(StockInfo.builder().sku(footlocerBaseInfo.getSku()).build());



        //在内存操作，写到浏览器
        ExcelWriter writer= ExcelUtil.getWriter(true);

        //自定义标题别名
        writer.addHeaderAlias("sku","货号");
        writer.addHeaderAlias("size","尺码");


        //默认配置
        writer.write(list,true);
        //设置content—type
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset:utf-8");

        //设置标题
        String fileName= URLEncoder.encode("用户信息","UTF-8");
        //Content-disposition是MIME协议的扩展，MIME协议指示MIME用户代理如何显示附加的文件。
        response.setHeader("Content-Disposition","attachment;filename="+fileName+".xlsx");
        ServletOutputStream outputStream= response.getOutputStream();

        //将Writer刷新到OutPut
        writer.flush(outputStream,true);
        outputStream.close();
        writer.close();
    }


}
