package com.footlocer.mon.manager;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ItpMonitorKr {

    public void monitor() {

        try {
            String url = "https://poticket.interpark.com/Book/BookSeatDetail.asp?GoodsCode=24011941&PlaceCode=24001019&PlaySeq=001&SeatGrade=&Block=307&TmgsOrNot=D2003&LocOfImage=24001019.gif&Tiki=N&UILock=Y&SessionId=24011941_M0000005312231724954798&BizCode=WEBBR&GoodsBizCode=56650&SeatCheckCnt=0&InterlockingGoods=";

            HttpResponse response = HttpRequest.get(url)
                    .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("accept-language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")
                    .header("priority", "u=0, i")
                    .header("sec-ch-ua", "\"Not)A;Brand\";v=\"99\", \"Google Chrome\";v=\"127\", \"Chromium\";v=\"127\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "\"Windows\"")
                    .header("sec-fetch-dest", "iframe")
                    .header("sec-fetch-mode", "navigate")
                    .header("sec-fetch-site", "same-origin")
                    .header("sec-fetch-user", "?1")
                    .header("upgrade-insecure-requests", "1")
                    .execute();

            // 获取响应内容
            String responseBody = response.body();

            // 打印响应内容
            System.out.println(responseBody);

            parseAvailableSeats(responseBody);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void parseAvailableSeats(String html) {

        // 使用Jsoup解析HTML
        Document doc = Jsoup.parse(html);

        // 遍历包含座位行的span标签 (例如 SeatT 类标签)
        Elements seats = doc.select("span.SeatN");

        // 遍历所有座位
        for (Element seat : seats) {
            // 获取座位的 title 属性
            String title = seat.attr("title");

            // 打印座位信息，包括 title 和行号
            System.out.println("座位标题: " + title);
        }


    }

}
