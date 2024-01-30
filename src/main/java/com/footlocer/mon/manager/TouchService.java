package com.footlocer.mon.manager;


import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.footlocer.mon.config.TouchProperties;
import com.footlocer.mon.dto.touch.*;
import com.footlocer.mon.entity.CnData;
import com.footlocer.mon.entity.ShoeExcle;
import com.footlocer.mon.entity.StockxData;
import com.footlocer.mon.entity.TouchSku;
import com.footlocer.mon.service.ICnDataService;
import com.footlocer.mon.service.IShoeExcleService;
import com.footlocer.mon.service.IStockxDataService;
import com.footlocer.mon.service.ITouchSkuService;
import com.footlocer.mon.util.PriceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

@Service
public class TouchService {

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
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ITouchSkuService touchSkuService;

    /**
     * 获取touch的账号密码
     *
     * @param username
     * @param password
     * @return
     */
    public String loginTouch(String username, String password) {
        String username1 = touchProperties.getUsername();
        String username2 = touchProperties.getPassword();

        return null;
    }

    //根据货号列表查询价格
    public List<ShoeExcle> check(List<String> skuList) {
        List<ShoeExcle> res = new ArrayList<>();
        HashMap headers = new HashMap<>();//存放请求头，可以存放多个请求头
        headers.put("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2NWE0ZWM2MTUxODQwNzZmNzMyY2U4OTgiLCJ1c2VybmFtZSI6ImxpamlhcWkiLCJpc0FkbWluIjpmYWxzZSwiYXV0aCI6dHJ1ZSwiZGlzYWJsZWQiOmZhbHNlLCJpYXQiOjE3MDUzMDcyMzYsImV4cCI6MTcwNzg5OTIzNn0.a-T3pHwkmTUi8uQLGEpBHq_qO_6_FUSPUItYjnJigxE");

        HashMap body = new HashMap<>();//存放参数
        body.put("A", 100);
        body.put("B", 200);

        for (String sku : skuList) {
            String url = "https://win.touchtouch.cc/api/getAllPrice?sku=" + sku + "&prices[]=%7B%22name%22:%22price1%22,%22type%22:%22stockxPrice%22,%22region%22:%22US%22%7D&prices[]=%7B%22name%22:%22price2%22,%22type%22:%22cnPrice%22,%22region%22:%22CN%22%7D";
            String result = HttpUtil.createGet(url).addHeaders(headers).execute().body();
            TouchList touchList = JSONObject.parseObject(result, TouchList.class);
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    // 输出差价信息
                    List<ShoeExcle> shoeExcles = comparePrices(touchList.getList());
                }
            });

            //res.addAll(shoeExcles);
            //加入间隔  随机休息
            try {
                int sleepTime = new Random().nextInt(21) + 10; // Random value between 10 to 30
                Thread.sleep(sleepTime * 1000); // Convert to milliseconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return res;
    }


    private List<ShoeExcle> comparePrices(List<Shoe> shoes) {
        List<ShoeExcle> shoeExcleList = new ArrayList<>();
        //String jobName = UUID.randomUUID().toString();
        String jobName = "1-25正式";

        for (Shoe shoe : shoes) {
            List<StockxData> stockXDataList = shoe.getStockxData();
            for (StockxData stockxData : stockXDataList) {
                stockxData.setSku(shoe.getSku());
                stockxData.setJobName(jobName);
            }
            List<CnData> cnDataList = shoe.getCnData();
            for (CnData cnData : cnDataList) {
                cnData.setSku(shoe.getSku());
                cnData.setJobName(jobName);
            }
            cnDataService.saveBatch(cnDataList);
            stockxDataService.saveBatch(stockXDataList);

            for (StockxData stockXData : stockXDataList) {
                for (CnData cnData : cnDataList) {
                    if (formatDouble(stockXData.getSize()).equals(cnData.getSizeUS())) {
                        // 计算差价
                        double stockxGive = priceUtil.calculateStockx(stockXData.getPriceUS());
                        double dewuGive = priceUtil.calculateDewu(cnData.getPtPrice() != 0 ? cnData.getPtPrice() : cnData.getPlusPrice());

                        double priceDifference = stockxGive - dewuGive;

                        //if (priceDifference > 30 && dewuGive > 0)
                        if (dewuGive > 0) {

                            // 输出差价信息
                            System.out.println("货号：" + shoe.getSku() +
                                    ", 三日销量: " + stockXData.getSalesAmount() +
                                    ", Size: " + stockXData.getSize() +
                                    ", StockX 到手: $" + stockxGive +
                                    ", 得物到手: $" + dewuGive +
                                    ", 差价: $" + priceDifference);

                            ShoeExcle shoeExcle = new ShoeExcle();
                            shoeExcle.setSku(shoe.getSku().replace(" ", "-"));
                            shoeExcle.setJobName(jobName);
                            shoeExcle.setSizeUs(cnData.getSizeUS());
                            shoeExcle.setSizeCn(cnData.getSize());
                            shoeExcle.setSaleAmount(stockXData.getSalesAmount());
                            shoeExcle.setStockxPrice(stockXData.getPriceUS());
                            shoeExcle.setStockxHandPrice(stockxGive);
                            shoeExcle.setDewuPrice(cnData.getPtPrice() != 0 ? cnData.getPtPrice() : cnData.getPlusPrice());
                            shoeExcle.setDewuHandPrice(dewuGive);
                            shoeExcle.setPriceDifference(priceDifference);
                            shoeExcle.setCreateTime(new Date());

                            shoeExcleService.save(shoeExcle);

                            //更新状态
                            touchSkuService.lambdaUpdate()
                                    .eq(TouchSku::getSku, shoe.getSku()
                                            .replace(" ", "-"))
                                    .set(TouchSku::getUpdateTime, new Date());
                        }
                    }
                }
            }
        }
        return shoeExcleList;
    }


    /**
     * 尺码处理  匹配
     *
     * @param value
     * @return
     */
    public static String formatDouble(double value) {
        // 转换为字符串
        String result = Double.toString(value);

        // 去掉尾部的0
        result = result.indexOf('.') > 0 ? result.replaceAll("0*$", "").replaceAll("\\.$", "") : result;

        // 如果仍然包含小数点，则保留一位小数
        if (result.contains(".")) {
            result = String.format("%.1f", value);
        }

        return result;
    }

}
