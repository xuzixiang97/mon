package com.footlocer.mon;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.footlocer.mon.entity.ShoeExcle;
import com.footlocer.mon.entity.TouchSku;
import com.footlocer.mon.manager.*;
import com.footlocer.mon.service.ITouchSkuService;
import com.footlocer.mon.util.ChromeDriverUtil;
import com.footlocer.mon.util.ExcleUtil;
import com.footlocer.mon.util.TxtUtil;
import com.footlocer.mon.util.XuHttpUtil;
import com.taobao.api.ApiException;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class MonApplicationTests {

    @Autowired
    private ItpMonitor itpMonitor;

    @Autowired
    private ItpMonitorKr itpMonitorKr;

    @Autowired
    private TouchService touchService;

    @Autowired
    private ITouchSkuService touchSkuService;

    @Autowired
    private TouchUpdateService touchUpdateService;

    @Autowired
    private GenHypeProxy genHypeProxy;

    @Autowired
    private OrderReportService orderReportService;

    @Autowired
    private  MailBuyerMonitor mailBuyerMonitor;

    @Test
    void contextLoads() {

        try {
            Mac hasher = Mac.getInstance("HmacSHA256");
            hasher.init(new SecretKeySpec("luD8CjX-1sSec58c10LhTw".getBytes(), "HmacSHA256"));
            byte[] hash = hasher.doFinal("h3JKHiJtUZ-yoA".getBytes());
            // to lowercase hexits
            DatatypeConverter.printHexBinary(hash);
            // to base64
            String sign = DatatypeConverter.printBase64Binary(hash);
            System.out.println(sign);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    void contextLoads1() {
        genHypeProxy.genHype();
    }

    @Test
    void contextLoads2() {
        //68.182.247.0:56811:wqbhnujg:9G7gh54uyM
        boolean wqbhnujg = genHypeProxy.isProxyWorking("68.182.247.0", 56821, "wqbhnujg", "9G7gh54uyM");
        System.out.println(wqbhnujg);
    }

    @Test
    void search() {
        List<String> skuList = new ArrayList<>();
        skuList.add("AQ9129-103");
        skuList.add("378038-170");

        //List<String> skuList = TxtUtil.readTxtFile("D:\\bot\\出口\\skuAll.txt");
        List<ShoeExcle> check = touchService.check(skuList);
    }

    @Test
    void insert() {
        List<String> skuList = TxtUtil.readTxtFile("D:\\bot\\出口\\skuAll.txt");
        List<TouchSku> touchSkuArrayList = new ArrayList<>();
        for (String sku : skuList) {
            TouchSku touchSku = new TouchSku();
            touchSku.setSku(sku);
            touchSkuArrayList.add(touchSku);
        }
        touchSkuService.saveBatch(touchSkuArrayList);
    }

    @Test
    void runTouchSku() {
        List<TouchSku> touchSkuList = touchSkuService.list();
        List<String> skuList = touchSkuList.stream().map(TouchSku::getSku).collect(Collectors.toList());
        List<ShoeExcle> check = touchService.check(skuList);
    }

    @Test
    void test6() throws IOException, InterruptedException {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\xuzixiang\\AppData\\Local\\Google\\Chrome\\Application\\chromedriver.exe");
        // 谷歌驱动
        ChromeOptions options = new ChromeOptions();
        // 允许所有请求
        options.addArguments("--remote-allow-origins=*");

        options.addArguments("--incognito"); //无痕模式
        options.addArguments("--user-agent=" + "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        ChromeDriver webDriver = new ChromeDriver(options);
        // 启动需要打开的网页
        webDriver.navigate().to("https://www.baidu.com");

    }

    @Test
    void test64() throws IOException, InterruptedException {
        ChromeDriverUtil build = ChromeDriverUtil.build("C:\\Users\\xuzixiang\\AppData\\Local\\Google\\Chrome\\Application\\chromedriver.exe");
        ChromeDriver driver = build.getDriver();
        driver.get("\n" +
                "https://www.finishline.com");
        String pageSource = driver.getPageSource();
        System.out.println(pageSource);


    }

    @Test
    void testupdate() throws IOException, InterruptedException, ApiException {
        List<ShoeExcle> shoeExcles = touchUpdateService.checkGuaShouList();

    }

    @Test
    void testupdate222() throws JsonProcessingException {
        mailBuyerMonitor.monitor();
        System.out.println("hhhh");;
    }


    @Test
    void testupdate333() throws JsonProcessingException {
        // 你抓包里的 Bearer token（已替换为你提供的完整值）
        String bearer = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4dXppeGlhbmc5N0BnbWFpbC5jb20iLCJleHAiOjE3NTg3NzE4MDIsInVzZXJJZCI6MTk3MDcxMTg3NzkyMTYyODE2MSwiaWF0IjoxNzU4NzY0NjAyLCJqdGkiOiI3MjdhOTk4ODQ4OGI0MTAxOGI2NjgwYjE4MmFhMWUwMiJ9.lUJDrEc3jy_h5c_1p6Da_0_Z4eokmMsxAE6IS_Ny4io";

        // 如果接口要求 Cookie，这里把浏览器里复制的一整串 Cookie 放进来；不需要可留空字符串
        String cookie = "";

        // 导出到项目根目录下的 CSV（也可给绝对路径）
        orderReportService.exportAllOrdersToCsv("all-orders.csv", bearer, cookie);
    }


}
