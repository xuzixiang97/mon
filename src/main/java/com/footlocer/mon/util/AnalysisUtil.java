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
    public static String parseFlusProductDetail(String html) throws IOException {

            Document doc = Jsoup.parse(html);

            Elements ee = doc.getElementsByTag("script");

            String text = "";

            for ( Element e : ee ) {
                String id = e.attributes().get("id");
                if(e.attributes().get("id").equals("productLdJson")){
                    String t = e.html();
                    System.out.println(t);
                }



            }

//        //String reg = "<script data-react-helmet=\"true\" type=\"application/ld+json\" id=\"productLdJson\">(.*?)</script>";//定义正则表达式
//        String pattern = "(<script data-react-helmet=\"true\" type=\"application/ld+json\" id=\"productLdJson\">)(.*?)(</script>)";
//        Pattern r = Pattern.compile(pattern);
//        // 创建 matcher 对象
//        Matcher m = r.matcher(html);
//        while (m.find()) {
//            /*
//             自动遍历打印所有结果   group方法打印捕获的组内容，以正则的括号角标从1开始计算，我们这里要第2个括号里的
//             值， 所以取 m.group(2)， m.group(0)取整个表达式的值，如果越界取m.group(4),则抛出异常
//           */
//            System.out.println("Found value: " + m.group(2));
//        }

        return html;
    }


}
