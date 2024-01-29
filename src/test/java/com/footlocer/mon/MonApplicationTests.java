package com.footlocer.mon;

import cn.hutool.http.HttpUtil;
import com.footlocer.mon.entity.ShoeExcle;
import com.footlocer.mon.manager.TouchService;
import com.footlocer.mon.util.ExcleUtil;
import com.footlocer.mon.util.TxtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class MonApplicationTests {

    @Autowired
    private TouchService touchService;

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

}
