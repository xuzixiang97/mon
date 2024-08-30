package com.footlocer.mon.manager;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItpMonitor {

    public void monitor() {

        try {
            // 基础URL
            String baseUrl = "https://gpoticket.globalinterpark.com/Global/Play/Book/BookSeatDetail.asp";

            // URL参数
            Map<String, Object> params = new HashMap<>();
            params.put("GoodsCode", "24011941");
            params.put("PlaceCode", "24001019");
            params.put("LanguageType", "G2001");
            params.put("MemBizCode", "10965");
            params.put("PlaySeq", "001");
            params.put("SeatGrade", "");
            params.put("Block", "412");
            params.put("TmgsOrNot", "D2003");
            params.put("LocOfImage", "24001019.gif");
            params.put("Tiki", "N");
            params.put("UILock", "Y");
            params.put("SessionId", "24011941_M0000005310771724953215");
            params.put("BizCode", "10965");
            params.put("GoodsBizCode", "56650");
            params.put("GlobalSportsYN", "N");
            params.put("SeatCheckCnt", "0");
            params.put("InterlockingGoods", "");

            // 发送GET请求并设置请求头
            HttpResponse response = HttpRequest.get(baseUrl)
                    .form(params)  // 添加URL参数
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("Accept-Encoding", "gzip, deflate, br, zstd")
                    .header("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")
                    .header("Cache-Control", "max-age=0")
                    .header("Cookie", "_fbp=fb.1.1724080648137.854579813192658078; _gid=GA1.2.880052056.1724918191; cbtUser=FpZdbrgm9PMHW7sRhkhSA7MwII+vFpxS7l/lysIIlTc=; memID=Z80hOIcRuDO0FNsKL%2F7PE7BuBkqNUWiELLUXcx8FEPk%3D; memNo=c4V4fYUqdnw%2BouX%2BkLVUYw%3D%3D; TMem%5FNO=T34006654; TMem%5FNO%5FG=T34006654; memEmail=Z80hOIcRuDO0FNsKL%2F7PE7BuBkqNUWiELLUXcx8FEPk%3D; _ga_BEN1B7STVY=GS1.1.1724949042.15.1.1724949419.0.0.0; _gat_UA-60117844-2=1; _ga=GA1.1.1739674172.1724080646; _ga_3840G72Z4Q=GS1.1.1724949042.14.1.1724950164.0.0.0")
                    .header("Priority", "u=0, i")
                    .header("Referer", "https://gpoticket.globalinterpark.com/Global/Play/Book/BookSeatDetail.asp?GoodsCode=24011941&PlaceCode=24001019&LanguageType=G2001&MemBizCode=10965&PlaySeq=001&SeatGrade=&Block=307&TmgsOrNot=D2003&LocOfImage=24001019.gif&Tiki=N&UILock=Y&SessionId=24011941_M0000005306511724950162&BizCode=10965&GoodsBizCode=56650&GlobalSportsYN=N&SeatCheckCnt=0&InterlockingGoods=")
                    .header("Sec-CH-UA", "\"Not)A;Brand\";v=\"99\", \"Google Chrome\";v=\"127\", \"Chromium\";v=\"127\"")
                    .header("Sec-CH-UA-Mobile", "?0")
                    .header("Sec-CH-UA-Platform", "\"Windows\"")
                    .header("Sec-Fetch-Dest", "iframe")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36")
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
