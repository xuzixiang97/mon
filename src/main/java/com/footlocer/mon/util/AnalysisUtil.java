package com.footlocer.mon.util;

import cn.hutool.json.JSONUtil;
import com.footlocer.mon.dto.FootlocerBaseInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class AnalysisUtil {


    //设置Proxy并返回
    public static FootlocerBaseInfo parseFlusProductBaseDetail(String html) throws IOException {

            Document doc = Jsoup.parse(html);

            Elements ee = doc.getElementsByTag("script");

            for ( Element e : ee ) {
                String id = e.attributes().get("id");
                if(e.attributes().get("id").equals("productLdJson")){
                    String t = e.html();
                    System.out.println(t);
                    boolean json = JSONUtil.isJson(t);
                    cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(t);
                    cn.hutool.json.JSONArray offers = (cn.hutool.json.JSONArray)jsonObject.get("offers");

                    FootlocerBaseInfo footlocerBaseInfo = JSONUtil.toBean(jsonObject, FootlocerBaseInfo.class);

                    return footlocerBaseInfo;

                }

            }


        return null;
    }



    //设置Proxy并返回
    public static FootlocerBaseInfo parseGoatItemInfo(String html) throws IOException {

        Document doc = Jsoup.parse(html);

        Elements ee = doc.getElementsByTag("script");
        Element next_data__ = doc.getElementById("__NEXT_DATA__");

        for ( Element e : ee ) {
            String id = e.attributes().get("id");
            if(e.attributes().get("id").equals("productLdJson")){
                String t = e.html();
                System.out.println(t);
                boolean json = JSONUtil.isJson(t);
                cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(t);
                cn.hutool.json.JSONArray offers = (cn.hutool.json.JSONArray)jsonObject.get("offers");

                FootlocerBaseInfo footlocerBaseInfo = JSONUtil.toBean(jsonObject, FootlocerBaseInfo.class);

                return footlocerBaseInfo;

            }

        }


        return null;
    }
    //设置Proxy并返回
    public static FootlocerBaseInfo parseCsusProductBaseDetail(String html) throws IOException {

        Document doc = Jsoup.parse(html);

        Elements ee = doc.getElementsByTag("script");

        for ( Element e : ee ) {
            String id = e.attributes().get("id");
            if(e.attributes().get("id").equals("productLdJson")){
                String t = e.html();
                System.out.println(t);
                boolean json = JSONUtil.isJson(t);
                cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(t);
                cn.hutool.json.JSONArray offers = (cn.hutool.json.JSONArray)jsonObject.get("offers");

                FootlocerBaseInfo footlocerBaseInfo = JSONUtil.toBean(jsonObject, FootlocerBaseInfo.class);

                return footlocerBaseInfo;

            }

        }


        return null;
    }



    public static String parseDetail(String html) throws IOException {

        Document doc = Jsoup.parse(html);

        Elements ee = doc.getElementsByTag("script");
        for (Element element : ee) {

            //todo 第四个js 解析详情


        }








        return html;
    }

}
