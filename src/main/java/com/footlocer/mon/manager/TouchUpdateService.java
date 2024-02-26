package com.footlocer.mon.manager;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.footlocer.mon.config.TouchProperties;
import com.footlocer.mon.dto.auto.*;
import com.footlocer.mon.dto.touch.Shoe;
import com.footlocer.mon.dto.touch.TouchList;
import com.footlocer.mon.entity.*;
import com.footlocer.mon.service.*;
import com.footlocer.mon.util.DingTalkPushUtil;
import com.footlocer.mon.util.PriceUtil;
import com.taobao.api.ApiException;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TouchUpdateService {

    @Autowired
    private TouchProperties touchProperties;

    @Autowired
    private PriceUtil priceUtil;

    @Autowired
    private IShoeExcleService shoeExcleService;

    @Autowired
    private ICnDataService cnDataService;

    @Autowired
    private IStockxDataService stockxDataService;

    @Autowired
    private ITouchSkuService touchSkuService;

    @Autowired
    private IBasepriceService basepriceService;


    //根据货号列表查询价格
    public List<ShoeExcle> checkGuaShouList() throws IOException, ApiException {
        List<ShoeExcle> res = new ArrayList<>();
        HashMap headers = new HashMap<>();//存放请求头，可以存放多个请求头
        headers.put("Authorization", touchProperties.getToken1());

        //获取挂售商品列表 信息
        String url = "https://win.touchtouch.cc/api/product/summaryList?type=agency&isNew=false&user=" + touchProperties.getUser1();
        String result = HttpUtil.createGet(url).addHeaders(headers).execute().body();
        GuaShouItemList guaShouItemList = JSONObject.parseObject(result, GuaShouItemList.class);

        //逐个遍历处理，进入改价页面改价
        for (GuaShouItem guaShouItem : guaShouItemList.getList()) {
            String url1 = "https://win.touchtouch.cc/api/product/version2?" +
                    "type=agency&page=1&pageSize=100&sku=" +
                    guaShouItem.getSku() +
                    "&isNew=false&sortName=size&sortType=1&user=" + touchProperties.getUser1();
            sleep();
            String result1 = HttpUtil.createGet(url1).addHeaders(headers).execute().body();
            ProductSellListing productSellListing = JSONObject.parseObject(result1, ProductSellListing.class);
            // 根据尺码去重  取每个尺码的最低价
            List<ProductSell> productSells = processSellListing(productSellListing);
            for (ProductSell productSell : productSells) {
                //判断是否需要改价
                List<Baseprice> basepriceList = basepriceService.lambdaQuery().eq(Baseprice::getSku, productSell.getSku())
                        .eq(Baseprice::getSize, productSell.getSize())
                        .list();
                if (CollectionUtil.isEmpty(basepriceList)) {
                    continue;
                }

                // 根据id 查价
                String url2 = "https://win.touchtouch.cc/api/stockxGoatPriceForAgencyProduct?productId=" + productSell.get_id();
                String result2 = HttpUtil.createGet(url2).addHeaders(headers).execute().body();
                Prices prices = JSONObject.parseObject(result2, PriceData.class).getPrices();
                //减8 $
                prices.setGoatStv(prices.getGoatStv() - 8);
                // stockx 改价
                if (productSell.getAgencySaleStockxPrice() > Integer.valueOf(prices.getStockxLowestAsk())) {
                    updateStockxPrice(productSell, prices, basepriceList.get(0));
                }
                //goat 改价
                if (productSell.getAgencySaleGoatPrice() > prices.getGoatStv()) {
                    updateGoatPrice(productSell, prices, basepriceList.get(0));
                }
            }
        }

        return res;
    }


    /**
     * 根据尺码去重  取每个尺码的最低价
     *
     * @return
     */
    public List<ProductSell> processSellListing(ProductSellListing productSellListing) {
        List<ProductSell> products = productSellListing.getList();
        Map<String, ProductSell> productMap = new HashMap<>();

        for (ProductSell product : products) {
            String size = product.getSize();
            if (!productMap.containsKey(size)) {
                productMap.put(size, product);
            } else {
                ProductSell existingProduct = productMap.get(size);
                if (product.getAgencySaleStockxPrice() < existingProduct.getAgencySaleStockxPrice()) {
                    existingProduct.setAgencySaleStockxPrice(product.getAgencySaleStockxPrice());
                }
                if (product.getAgencySaleGoatPrice() < existingProduct.getAgencySaleGoatPrice()) {
                    existingProduct.setAgencySaleGoatPrice(product.getAgencySaleGoatPrice());
                }
            }
        }

        return new ArrayList<>(productMap.values());

    }


    //
    private void updateGoatPrice(ProductSell productSell, Prices prices, Baseprice baseprice) throws IOException, ApiException {

        if (productSell.getAgencySaleGoatPrice() > prices.getGoatStv() && prices.getGoatStv() > baseprice.getGoatLow()) {
            //触发改价
            Integer newPrice = prices.getGoatStv() - 1;
            String url = "https://win.touchtouch.cc/api/product/updateGoatPrice";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("productId", productSell.get_id());
            jsonObject.put("price", newPrice);
            sendPutRequest(url, jsonObject.toJSONString(), touchProperties.getToken1());
            String message = getnow() + " goat执行改价上架 id 为：" + productSell.get_id() + "货号为：" + productSell.getSku()
                    + "尺码为：" + productSell.getSize() + "原价为" + productSell.getAgencySaleGoatPrice() + "修改后价格为" + newPrice + "$";
            System.out.println(message);
            DingTalkPushUtil.pushTouch(message, productSell, productSell.getAgencySaleGoatPrice(), newPrice, "Goat", baseprice.getGoatLow());
            sleep();
        }


    }

    private void updateStockxPrice(ProductSell productSell, Prices prices, Baseprice baseprice) throws IOException, ApiException {


        if (productSell.getAgencySaleStockxPrice() > Integer.valueOf(prices.getStockxLowestAsk()) && Integer.valueOf(prices.getStockxLowestAsk()) > baseprice.getStockxLow()) {
            //触发改价
            Integer newPrice = Integer.valueOf(prices.getStockxLowestAsk()) - 1;
            String url = "https://win.touchtouch.cc/api/product/updateStockxPrice";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("productId", productSell.get_id());
            jsonObject.put("price", newPrice);
            sendPutRequest(url, jsonObject.toJSONString(), touchProperties.getToken1());
            String message = getnow() + " stockx执行改价上架 id 为：" + productSell.get_id() + "货号为：" + productSell.getSku()
                    + "尺码为：" + productSell.getSize() + "原价为" + productSell.getAgencySaleStockxPrice() + "修改后价格为" + newPrice + "$";
            System.out.println(message);
            DingTalkPushUtil.pushTouch(message, productSell, productSell.getAgencySaleStockxPrice(), newPrice, "StockX", baseprice.getStockxLow());
            sleep();
        }

    }


    public String httpHutoolPut(String url, Map<String, String> map, JSONObject jsons) {
        String object = HttpRequest.put(url).addHeaders(map).form(jsons).execute().body();
        return object;
    }

    public static void sendPutRequest(String url, String jsonData, String token) throws IOException {
        URL urlObj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

        // 设置请求方法为PUT
        conn.setRequestMethod("PUT");

        // 设置请求头部信息
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", token);

        // 启用输出流
        conn.setDoOutput(true);

        // 写入请求数据
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonData.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // 读取响应
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println("Response from server: " + response.toString());
        }

        // 关闭连接
        conn.disconnect();
    }

    private void sleep() {
        //加入间隔  随机休息
        try {
            int sleepTime = new Random().nextInt(11) + 5; // Random value between 5 to 15
            Thread.sleep(sleepTime * 1000); // Convert to milliseconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getnow() {
        // 获取当前时间
        LocalDateTime currentTime;
        currentTime = LocalDateTime.now();

        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 格式化日期时间为字符串
        String formattedTime = currentTime.format(formatter);

        // 输出字符串
        return formattedTime;
    }


}
