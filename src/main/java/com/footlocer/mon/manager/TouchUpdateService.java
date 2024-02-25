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
import com.footlocer.mon.util.PriceUtil;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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
    public List<ShoeExcle> checkGuaShouList() {
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
            String result1 = HttpUtil.createGet(url1).addHeaders(headers).execute().body();
            ProductSellListing productSellListing = JSONObject.parseObject(result1, ProductSellListing.class);
            // 根据尺码去重  取每个尺码的最低价
            List<ProductSell> productSells = processSellListing(productSellListing);
            for (ProductSell productSell : productSells) {
                // 根据id 查价
                String url2 = "https://win.touchtouch.cc/api/stockxGoatPriceForAgencyProduct?productId=" + productSell.get_id();
                String result2 = HttpUtil.createGet(url2).addHeaders(headers).execute().body();
                Prices prices = JSONObject.parseObject(result, PriceData.class).getPrices();
                // stockx 改价
                if (productSell.getAgencySaleStockxPrice() > Integer.valueOf(prices.getStockxLowestAsk())) {
                    updateStockxPrice(productSell, prices);
                }
                //goat 改价
                if (productSell.getAgencySaleGoatPrice() > prices.getGoatStv()) {
                    updateGoatPrice(productSell, prices);
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
    private void updateGoatPrice(ProductSell productSell, Prices prices) {
        List<Baseprice> basepriceList = basepriceService.lambdaQuery().eq(Baseprice::getSku, productSell.getSku())
                .eq(Baseprice::getSize, productSell.getSize())
                .list();
        if (CollectionUtil.isEmpty(basepriceList)) {
            return;
        } else {
            //改价逻辑
            Baseprice baseprice = basepriceList.get(0);
            if (productSell.getAgencySaleGoatPrice() > prices.getGoatStv() && prices.getGoatStv() > baseprice.getGoatLow()) {
                //触发改价
                Integer newPrice = prices.getGoatStv() - 1;
                HashMap headers = new HashMap<>();//存放请求头，可以存放多个请求头
                headers.put("Authorization", touchProperties.getToken1());
                String url = "https://win.touchtouch.cc/api/product/updateGoatPrice";
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("productId", productSell.get_id());
                jsonObject.put("price", newPrice);
                String result = httpHutoolPut(url, headers, jsonObject);
                sleep();
            }
        }

    }

    private void updateStockxPrice(ProductSell productSell, Prices prices) {
        List<Baseprice> basepriceList = basepriceService.lambdaQuery().eq(Baseprice::getSku, productSell.getSku())
                .eq(Baseprice::getSize, productSell.getSize())
                .list();
        if (CollectionUtil.isEmpty(basepriceList)) {
            return;
        } else {
            //改价逻辑
            Baseprice baseprice = basepriceList.get(0);
            if (productSell.getAgencySaleStockxPrice() > Integer.valueOf(prices.getStockxLowestAsk()) && Integer.valueOf(prices.getStockxLowestAsk()) > baseprice.getStockxLow()) {
                //触发改价
                Integer newPrice = prices.getGoatStv() - 1;
                HashMap headers = new HashMap<>();//存放请求头，可以存放多个请求头
                headers.put("Authorization", touchProperties.getToken1());
                String url = "https://win.touchtouch.cc/api/product/updateStockxPrice";
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("productId", productSell.get_id());
                jsonObject.put("price", newPrice);
                String result = httpHutoolPut(url, headers, jsonObject);
                sleep();
            }
        }
    }


    public String httpHutoolPut(String url, Map<String, String> map, JSONObject jsons) {
        String object = HttpRequest.put(url).addHeaders(map).form(jsons).execute().body();
        return object;
    }

    private void sleep(){
        //加入间隔  随机休息
        try {
            int sleepTime = new Random().nextInt(21) + 10; // Random value between 10 to 30
            Thread.sleep(sleepTime * 1000); // Convert to milliseconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
