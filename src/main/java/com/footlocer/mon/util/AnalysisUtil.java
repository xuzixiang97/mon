package com.footlocer.mon.util;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalysisUtil {


    //设置Proxy并返回
    public static String parseFlusProductBaseDetail(String html) throws IOException {

            Document doc = Jsoup.parse(html);

            Elements ee = doc.getElementsByTag("script");
        Elements dd = doc.getElementsByTag("body");

            String text = "";

            for ( Element e : ee ) {
                String id = e.attributes().get("id");
                if(e.attributes().get("id").equals("productLdJson")){
                    String t = e.html();
                    System.out.println(t);
                }



            }


        return html;
    }


    public static String parseDetail(String html) throws IOException {

        Document doc = Jsoup.parse(html);

        Elements ee = doc.getElementsByTag("script");
        Elements dd = doc.getElementsByTag("body");


        for ( Element d : dd ) {
            Elements script = d.getElementsByTag("script");
           //获取第一个里面的信息
        }



        return html;
    }

}
