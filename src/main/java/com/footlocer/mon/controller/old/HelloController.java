package com.footlocer.mon.controller.old;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.footlocer.mon.dto.FootlocerBaseInfo;
import com.footlocer.mon.dto.Item;
import com.footlocer.mon.dto.Offers;
import com.footlocer.mon.dto.StockInfo;
import com.footlocer.mon.util.*;
import com.taobao.api.ApiException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
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
    public void hello1(HttpServletResponse response) throws Exception {

        List<StockInfo> resultlist = new ArrayList<>();

        //读取 货号列表
        List<Item> itemList = readExcel();

        for (Item item : itemList) {

            //处理footlocker 页面货号
            String footlockerUrl = "https://www.footlocker.com/en/product/~/"+ item.getSku()+".html";
            // todo 换代理  读取代理文件
            String footlockerHtml = XuHttpUtil.getHtml2(footlockerUrl);
            FootlocerBaseInfo footlocerBaseInfo = AnalysisUtil.parseFlusProductBaseDetail(footlockerHtml);
            List<String> footlockerSize = new ArrayList<>();
            StringBuilder sizeList = new StringBuilder();

            if(footlocerBaseInfo != null && footlocerBaseInfo.getOffers() != null){
                for (Offers offer : footlocerBaseInfo.getOffers()) {
                    if(offer.getAvailability().equals("InStock")){
                        footlockerSize.add(StrUtil.removePrefix(offer.getSku(), item.getSku() + "-"));
                        sizeList.append(StrUtil.removePrefix(offer.getSku(), item.getSku() + "-")).append(",");
                    }
                }
            }

            //处理ChampsSport 页面货号
            //https://www.champssports.com/product/nike-lebron-xx-mens/J5423001.html
            String champsSportYrl = "https://www.champssports.com/product/~/"+ item.getSku()+".html";
            String champssportHtml = XuHttpUtil.getHtml2(champsSportYrl);
            FootlocerBaseInfo champssportBaseInfo = AnalysisUtil.parseFlusProductBaseDetail(champssportHtml);
            List<String> champssportSize = new ArrayList<>();
            StringBuilder sizeList2 = new StringBuilder();

            if(champssportBaseInfo != null && champssportBaseInfo.getOffers() != null){
                for (Offers offer : champssportBaseInfo.getOffers()) {
                    if(offer.getAvailability().equals("InStock")){
                        champssportSize.add(StrUtil.removePrefix(offer.getSku(), item.getSku() + "-"));
                        sizeList2.append(StrUtil.removePrefix(offer.getSku(), item.getSku() + "-")).append(",");
                    }
                }
            }


//            //处理kid 页面货号
//            //https://www.champssports.com/product/nike-lebron-xx-mens/J5423001.html
//            String kidUrl = "https://www.kidsfootlocker.com/product/~/"+ item.getSku()+".html";
//            String kidHtml = XuHttpUtil.getHtml2(kidUrl);
//            FootlocerBaseInfo kidBaseInfo = AnalysisUtil.parseFlusProductBaseDetail(kidHtml);
//            List<String> kidSize = new ArrayList<>();
//            StringBuilder sizeList3 = new StringBuilder();
//
//            if(kidBaseInfo != null && kidBaseInfo.getOffers() != null){
//                for (Offers offer : kidBaseInfo.getOffers()) {
//                    if(offer.getAvailability().equals("InStock")){
//                        kidSize.add(StrUtil.removePrefix(offer.getSku(), item.getSku() + "-"));
//                        sizeList3.append(StrUtil.removePrefix(offer.getSku(), item.getSku() + "-")).append(",");
//                    }
//                }
//            }



//            //构建 list 输出结果
//            resultlist.add(StockInfo.builder().sku(item.getSku())
//                            .footlockerSize(sizeList.toString())
//                            .champssportSize(sizeList2.toString())
//                            .kidSize(sizeList3.toString())
//                    .build());

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


    private List<Item> readExcel2() throws FileNotFoundException {
        File file=new File("C:\\Users\\Administrator\\Desktop\\monit.xlsx");
        // 1.获取上传文件输入流
        InputStream inputStream = new FileInputStream(file);
        // 2.应用HUtool ExcelUtil获取ExcelReader指定输入流和sheet
        ExcelReader excelReader = ExcelUtil.getReader(inputStream, "footlocker");
        // 可以加上表头验证
        // 3.读取第二行到最后一行数据
        List<Item> items = excelReader.readAll(Item.class);
        return items;
    }
    @GetMapping("/test2")
    public String queryGoat(String name) throws Exception {

        //处理footlocker 页面货号
        //
        String footlockerUrl = "https://www.goat.com/sneakers/550-white-summer-fog-bb550ncb";
        // todo 换代理  读取代理文件
        String goatHtml = XuHttpUtil.getHtml2(footlockerUrl);
        AnalysisUtil.parseGoatItemInfo(goatHtml);

        return null;
    }

    @GetMapping("/test3")
    public String queryGoat2(String name) throws IOException {
        // 指定火狐浏览器安装位置
        //System.setProperty("webdriver.firefox.bin", "C:\\Program Files\\Mozilla Firefox");
        // 指定selenium 火狐浏览器驱动程序位置
        //System.setProperty("webdriver.gecko.driver", "C:\\Program Files\\Mozilla Firefox");
        System.setProperty("webdriver.gecko.driver", "C:\\Program Files\\Mozilla Firefox\\geckodriver.exe");

        // 获取火狐浏览器驱动对象
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setHeadless(true);
        firefoxOptions.addArguments("--no-sandbox");
        firefoxOptions.addArguments("--disable-gpu");
        firefoxOptions.addArguments("--disable-dev-shm-usage");

        WebDriver  driver = new FirefoxDriver(firefoxOptions);
        driver.get("https://www.baidu.com/");


        return null;
    }


    @GetMapping("/test4")
    public String queryGoat4(String name) throws Exception {
        List<StockInfo> resultlist = new ArrayList<>();

        //读取 货号列表
        List<Item> itemList = readExcel2();
        for (Item item : itemList) {

            //处理footlocker 页面货号
            String footlockerUrl = "https://www.footlocker.com/en/product/~/"+ item.getSku()+".html";
            // todo 换代理  读取代理文件
            String footlockerHtml = XuHttpUtil.getHtml2(footlockerUrl);
            FootlocerBaseInfo footlocerBaseInfo = AnalysisUtil.parseFlusProductBaseDetail(footlockerHtml);
            List<String> footlockerSize = new ArrayList<>();
            StringBuilder sizeList = new StringBuilder();
            if(footlocerBaseInfo != null && footlocerBaseInfo.getOffers() != null){
                for (Offers offer : footlocerBaseInfo.getOffers()) {
                    if(offer.getAvailability().equals("InStock")){
                        footlockerSize.add(StrUtil.removePrefix(offer.getSku(), item.getSku() + "-"));
                        sizeList.append(StrUtil.removePrefix(offer.getSku(), item.getSku() + "-")).append(",");
                    }
                }
                footlocerBaseInfo.setUrl(footlockerUrl);
                footlocerBaseInfo.setSizeSum(sizeList.toString());
                DingTalkPushUtil.pushText(footlocerBaseInfo);
            }


            //处理ChampsSport 页面货号
            //https://www.champssports.com/product/nike-lebron-xx-mens/J5423001.html
            String champsSportYrl = "https://www.champssports.com/product/~/"+ item.getSku()+".html";
            String champssportHtml = XuHttpUtil.getHtml2(champsSportYrl);
            FootlocerBaseInfo champssportBaseInfo = AnalysisUtil.parseFlusProductBaseDetail(champssportHtml);
            List<String> champssportSize = new ArrayList<>();
            StringBuilder sizeList2 = new StringBuilder();

            if(champssportBaseInfo != null && champssportBaseInfo.getOffers() != null){
                for (Offers offer : champssportBaseInfo.getOffers()) {
                    if(offer.getAvailability().equals("InStock")){
                        champssportSize.add(StrUtil.removePrefix(offer.getSku(), item.getSku() + "-"));
                        sizeList2.append(StrUtil.removePrefix(offer.getSku(), item.getSku() + "-")).append(",");
                    }
                }
                champssportBaseInfo.setUrl(champsSportYrl);
                champssportBaseInfo.setSizeSum(sizeList2.toString());
                DingTalkPushUtil.pushText(footlocerBaseInfo);
            }

        }



        return null;
    }




}
