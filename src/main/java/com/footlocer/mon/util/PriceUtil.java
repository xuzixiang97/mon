package com.footlocer.mon.util;

import cn.hutool.core.util.StrUtil;
import com.footlocer.mon.config.TouchProperties;
import com.footlocer.mon.dto.Proxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class PriceUtil {

    @Autowired
    private TouchProperties touchProperties;

    /**
     * 计算stockx到手价格
     * @param
     * @return
     */
    public double calculateStockx(int price) {
        double totalPrice = price *0.905;
        return totalPrice;
    }

    /**
     * 计算得物到手价格  $  已换算成美元
     * 平台最低价 * (100% - 3.5% - 1%) - 38 - 8.5
     * @param
     * @return
     */
    public double calculateDewu(int price) {
        double totalPrice = price / 100 * 0.955 -38 - 8.9;
        return totalPrice / touchProperties.getUsd();
    }



}
