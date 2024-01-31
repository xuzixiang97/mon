package com.footlocer.mon;

import cn.hutool.http.HttpUtil;
import com.footlocer.mon.entity.ShoeExcle;
import com.footlocer.mon.entity.TouchSku;
import com.footlocer.mon.manager.TouchService;
import com.footlocer.mon.service.ITouchSkuService;
import com.footlocer.mon.util.ChromeDriverUtil;
import com.footlocer.mon.util.ExcleUtil;
import com.footlocer.mon.util.TxtUtil;
import com.footlocer.mon.util.XuHttpUtil;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class MonApplicationTests {

    @Autowired
    private TouchService touchService;

    @Autowired
    private ITouchSkuService touchSkuService;

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
        System.setProperty("webdriver.chrome.driver","C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
        WebDriver driver= new ChromeDriver(new ChromeDriverService.Builder().usingPort(64534).build());
        driver.get("https://www.bootteer.com/user/order/index.html");

    }

    @Test
    void test46() throws IOException, InterruptedException {
        ChromeDriverUtil build = ChromeDriverUtil.build("https://www.footlocker.com/category/new-arrivals.html");

    }


}
