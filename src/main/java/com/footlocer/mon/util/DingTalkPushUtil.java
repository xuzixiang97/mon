package com.footlocer.mon.util;

import cn.hutool.core.date.DateUnit;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.footlocer.mon.dto.FootlocerBaseInfo;
import com.footlocer.mon.dto.auto.Prices;
import com.footlocer.mon.dto.auto.ProductSell;
import com.taobao.api.ApiException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class DingTalkPushUtil {

    /**
     * 按照钉钉API处理内容格式
     *
     * @param
     */
    public static void pushText(FootlocerBaseInfo footlocerBaseInfo) throws ApiException {
        push2(footlocerBaseInfo);
    }

    /**
     * 推送消息
     *
     * @param obj
     */
    public static void push(Object obj) {
        try {
            //自定义钉钉机器人生成链接  access_token钉钉自动生成
            URL url = new URL("https://oapi.dingtalk.com/robot/send?access_token=a506139f365bc2665e845b2abcbbc312b133c1b19718db37540e53353809fe3f");
            //打开连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // Post 请求不能使用缓存
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/Json; charset=UTF-8");
            conn.connect();
            OutputStream out = conn.getOutputStream();
            String jsonMessage = JSONObject.toJSONString(obj);
            byte[] data = jsonMessage.getBytes();
            // 发送请求参数
            out.write(data);
            // flush输出流的缓冲
            out.flush();
            out.close();
            //开始获取数据
            InputStream in = conn.getInputStream();
            byte[] content = new byte[in.available()];
            in.read(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void push2(FootlocerBaseInfo footlocerBaseInfo) throws ApiException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/robot/send?access_token=a506139f365bc2665e845b2abcbbc312b133c1b19718db37540e53353809fe3f");
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("markdown");
        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
        markdown.setTitle("杭州天气monit");
        markdown.setText("#### monit" + footlocerBaseInfo.getUrl() + "\n" +
                "> 尺码信息" + footlocerBaseInfo.getSizeSum() + "\n\n" +
                "> ![screenshot](" + footlocerBaseInfo.getImage() + ")\n" +
                "> ###### " + sdf.format(new Date()) + " [页面](" + footlocerBaseInfo.getUrl() + ") \n");
        request.setMarkdown(markdown);
        OapiRobotSendResponse response = client.execute(request);
    }

    public static void pushTouch(String message, ProductSell productSell, Integer oldPrice, Integer newPrice, String pingTai, Integer dijia) throws ApiException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/robot/send?access_token=a506139f365bc2665e845b2abcbbc312b133c1b19718db37540e53353809fe3f");
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("markdown");
        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
        markdown.setTitle("杭州天气monit");
        markdown.setText("#### touch" + "\n" +
                "> sku：" + productSell.getSku() + "\n" + "<br>" +
                "> 尺码：" + productSell.getSize() + "\n" + "<br>" +
                "> 平台：" + pingTai + "\n" + "<br>" +
                "> 原价：" + oldPrice + "$" + "\n" + "<br>" +
                "> 修改为：" + newPrice + "$" + "\n" + "<br>" +
                "> 底价：" + dijia + "$" + "\n" + "<br>" +
                "![screenshot](" + productSell.getPictureUrl() + ")\n" +
                "> ###### " + sdf.format(new Date()) + "\n");
        // markdown.setText(message);
        request.setMarkdown(markdown);
        OapiRobotSendResponse response = client.execute(request);
    }

}
