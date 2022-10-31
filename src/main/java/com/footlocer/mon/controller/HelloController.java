package com.footlocer.mon.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.footlocer.mon.dto.FootlocerBaseInfo;
import com.footlocer.mon.dto.Item;
import com.footlocer.mon.dto.Offers;
import com.footlocer.mon.dto.StockInfo;
import com.footlocer.mon.util.AnalysisUtil;
import com.footlocer.mon.util.ExcleUtil;
import com.footlocer.mon.util.XuHttpUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
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

        List<StockInfo> resultlist = new ArrayList<>();

        //读取 货号列表
        List<Item> itemList = readExcel();

        for (Item item : itemList) {

            String skuIrl = "https://www.footlocker.com/en/product/~/"+ item.getSku()+".html";
            // todo 换代理  读取代理文件
            String html = XuHttpUtil.getHtml(skuIrl);
            FootlocerBaseInfo footlocerBaseInfo = AnalysisUtil.parseFlusProductBaseDetail(html);

            StringBuilder sizeList = new StringBuilder();

            if(footlocerBaseInfo != null && footlocerBaseInfo.getOffers() != null){
                for (Offers offer : footlocerBaseInfo.getOffers()) {
                    if(offer.getAvailability().equals("InStock")){
                        sizeList.append(StrUtil.removePrefix(item.getSku(), item.getSku() + "-")).append(",");
                    }
                }
            }

            //构建 list 输出结果
            resultlist.add(StockInfo.builder().sku(footlocerBaseInfo.getSku())
                            .size(sizeList.toString())
                    .build());

        }


        //输出 html
        ExcleUtil.exportFootlocer(resultlist, response);


    }


    private List<Item> readExcel() throws FileNotFoundException {
        File file=new File("C:\\Users\\Administrator\\Desktop\\test.xlsx");

        // 1.获取上传文件输入流
        InputStream inputStream = new FileInputStream(file);
        // 2.应用HUtool ExcelUtil获取ExcelReader指定输入流和sheet
        ExcelReader excelReader = ExcelUtil.getReader(inputStream, "footlocker");
        // 可以加上表头验证
        // 3.读取第二行到最后一行数据
        List<Item> items = excelReader.readAll(Item.class);


        return items;
    }


}
