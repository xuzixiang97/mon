package com.footlocer.mon;

import com.footlocer.mon.service.TouchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class MonApplicationTests {

    @Autowired
    private TouchService touchService;

    @Test
    void contextLoads() {
        touchService.loginTouch("aaa" , "bbb");
    }

    @Test
    void search() {
        List<String> skuList = new ArrayList<>();
        skuList.add("AQ9129-103");
        skuList.add("378038-170");
        touchService.check(skuList);
    }

}
