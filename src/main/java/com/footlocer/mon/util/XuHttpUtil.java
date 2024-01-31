package com.footlocer.mon.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class XuHttpUtil {

    public static String getHtml(String url) throws IOException {

        // todo 测试代理能不能访问四大
        //  todo 自动切换代理

        URL url2 = new URL(url);

        // 构建传入代理连接的ip、port和用户名密码
//        String hostname="46.37.119.1";
//        String  port = "63400";
//        String authUser = "ykzwedby";
//        String authPassword = "8b3og68JoV";
        //us-30m.geosurf.io:8000:626761+US+626761-548256714878:ieYdzl
        //us-30m.geosurf.io:8000:626761+US+626761-633097021453:ieYdzl
        List<String> authList = new ArrayList<>();
        String hostname="us-30m.geosurf.io";
        String  port = "8000";
        String authUser = "626761+US+626761-633097021453";
        String authPassword = "ieYdzl";
        authList.add(0,hostname);
        authList.add(1,port);
        authList.add(2,authUser);
        authList.add(3,authPassword);
        //通过代理进行连接

        URLConnection urlConnection = url2.openConnection(getProxy(authList));
        urlConnection.setRequestProperty("User-Agent", "Mozilla/4.76");

        //构建input输入流
        InputStream input = urlConnection.getInputStream();
        //将内容打印至控制台
        String res = getContent(input);
        //System.out.println(res);
        return res;
    }


    public static String getHtml2(String url) throws Exception {
// 设置webClient的相关参数
        // todo 测试代理能不能访问四大
        //  todo 自动切换代理

        URL url2 = new URL(url);

        // 构建传入代理连接的ip、port和用户名密码
//        String hostname="46.37.119.1";
//        String  port = "63400";
//        String authUser = "ykzwedby";
//        String authPassword = "8b3og68JoV";
        //us-30m.geosurf.io:8000:626761+US+626761-548256714878:ieYdzl
        //us-30m.geosurf.io:8000:626761+US+626761-633097021453:ieYdzl

        List<String> proxyList = ProxyUtil.getFileContext("C:\\work\\proxy.txt");
        List<com.footlocer.mon.dto.Proxy> proxies = ProxyUtil.excuteProxyList(proxyList);
        for (int i = 0; i < proxyList.size(); i++) {
            try {
                com.footlocer.mon.dto.Proxy proxy = proxies.get(0);
                List<String> authList = new ArrayList<>();
                String hostname=proxy.getHostname();
                String  port = proxy.getPort();
                String authUser = proxy.getAuthUser();
                String authPassword = proxy.getAuthPassword();
                authList.add(0,hostname);
                authList.add(1,port);
                authList.add(2,authUser);
                authList.add(3,authPassword);
                //通过代理进行连接
                URLConnection urlConnection = url2.openConnection(getProxy(authList));
                urlConnection.setRequestProperty("User-Agent", "Mozilla/4.76");
                //构建input输入流
                InputStream input = urlConnection.getInputStream();
                //将内容打印至控制台
                String res = getContent(input);
                //System.out.println(res);
                return res;
            } catch (Exception e) {
                System.out.println("业务执行出现异常，e: " + e);
                Thread.sleep(200);
                return null;
            }
        }

        return "";
    }

    public static String getHtml3(String url1) throws IOException {

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String line = null;
        try {
            URL url = new URL(url1);
            // 根据URL生成HttpURLConnection
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");// 默认GET请求
            //connection.setDoInput(true);
            ///connection.setDoOutput(true);
            //connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            //模拟postman
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36");
            //connection.setRequestProperty("cookie", "_scid=193cabf6-fd1b-4c6d-93ac-cb1cb4a959be; _csrf=J7CAKREI7wh0oqTwvnhah_T9; country=US; currency=USD; guestCheckoutCohort=84; global_pricing_regions={\"AT\":\"2\",\"BE\":\"2\",\"BG\":\"2\",\"CY\":\"2\",\"CZ\":\"2\",\"DE\":\"2\",\"DK\":\"2\",\"EE\":\"2\",\"ES\":\"2\",\"FI\":\"2\",\"FR\":\"2\",\"GR\":\"2\",\"HK\":\"223\",\"HR\":\"2\",\"HU\":\"2\",\"IE\":\"2\",\"IT\":\"2\",\"JP\":\"57\",\"LT\":\"2\",\"LU\":\"2\",\"LV\":\"2\",\"MT\":\"2\",\"MY\":\"69\",\"NL\":\"2\",\"PL\":\"2\",\"PT\":\"2\",\"RO\":\"2\",\"SE\":\"2\",\"SG\":\"106\",\"SI\":\"2\",\"SK\":\"2\",\"UK\":\"4\",\"US\":\"3\"}; global_pricing_id=3; ConstructorioID_client_id=2b4b1763-2d26-45f1-9a21-487d4b8b311f; _gcl_au=1.1.371175090.1667919512; _tt_enable_cookie=1; _ttp=f64ef128-8f71-4132-bcae-d9ccba7303bf; ConstructorioID_session_id=3; _gid=GA1.2.576221669.1668225207; __cf_bm=vi6AF_vi5a0tYIRCQ3YQ8s1W5rLV68zzo87sUnBsxE4-1668225207-0-AUXuTZYch/IqEEgbzVwglERy3+Iju4YlmTXF3DEO5xBKr65wZJNyyFujMMycUrhavCF0xVD3p3lZsqpwq9JTm6MzHmUNmoHvBUSPLmrPhT9JiAOupRXxxWdatuYa5dgBRb4tsfLP7tMdZbXTBQh+fxgIZH9WxSfB2OzKyJDFFgmX; ConstructorioID_session={\"sessionId\":3,\"lastTime\":1668225631762}; csrf=mBkKQ9RR-B6Jl4JlRo2Ep0nf0QPuiMgqRk2k; _ga_28GTCC4968=GS1.1.1668225207.4.0.1668225632.0.0.0; _ga=GA1.1.413204090.1667919512; OptanonConsent=isIABGlobal=false&datestamp=Sat+Nov+12+2022+04%3A00%3A32+GMT%2B0000+(%E5%8D%8F%E8%B0%83%E4%B8%96%E7%95%8C%E6%97%B6)&version=6.10.0&hosts=&consentId=b5cf09b3-5683-42be-ad12-27ea647c7b0b&interactionCount=0&landingPath=https%3A%2F%2Fwww.goat.com%2Fsneakers%2F550-white-summer-fog-bb550ncb&groups=C0001%3A1%2CC0002%3A1%2CC0003%3A1%2CC0004%3A1");

            connection.connect();// 建立TCP连接
            int type = connection.getResponseCode();

                // 发送http请求
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                StringBuilder result = new StringBuilder();
                // 循环读取流
                while ((line = reader.readLine()) != null) {
                    result.append(line).append(System.getProperty("line.separator"));// "\n"
                }
                System.out.println(result.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connection.disconnect();

        }
        return "";
    }




    public static String getHtml4(String url) throws IOException {

        // todo 测试代理能不能访问四大
        //  todo 自动切换代理

        URL url2 = new URL(url);

        // 构建传入代理连接的ip、port和用户名密码
//        String hostname="46.37.119.1";
//        String  port = "63400";
//        String authUser = "ykzwedby";
//        String authPassword = "8b3og68JoV";
        //us-30m.geosurf.io:8000:626761+US+626761-548256714878:ieYdzl
        List<String> authList = new ArrayList<>();
        String hostname="us-30m.geosurf.io";
        String  port = "8000";
        String authUser = "626761+US+626761-548256714878";
        String authPassword = "ieYdzl";
        authList.add(0,hostname);
        authList.add(1,port);
        authList.add(2,authUser);
        authList.add(3,authPassword);


        //通过代理进行连接
        URLConnection urlConnection = url2.openConnection(getProxy(authList));

        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        urlConnection.setRequestProperty("accept", "*/*");
        urlConnection.setRequestProperty("connection", "Keep-Alive");
        //模拟postman
        urlConnection.setRequestProperty("User-Agent", "PostmanRuntime/7.26.8");

        //构建input输入流
        InputStream input = urlConnection.getInputStream();
        //将内容打印至控制台
        String res = getContent(input);
        //System.out.println(res);
        return res;
    }




    //设置Proxy并返回
    private static  Proxy getProxy(List<String> authList){

        String hostname = authList.get(0);
        int port = Integer.parseInt(authList.get(1));
        String authUser = authList.get(2);
        String authPassword = authList.get(3);
        //设置认证信息
        setAuthProperties(authUser,authPassword);

        //构造proxy的地址和端口并返回
        SocketAddress socketAddress = new InetSocketAddress(hostname,port);
        Proxy proxy = new Proxy(Proxy.Type.HTTP,socketAddress);
        return proxy;
    }


    //设置认证相关信息
    private static void setAuthProperties(String authUser, String authPassword) {
        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(authUser, authPassword.toCharArray());
                    }
                }
        );
    }

    //获取网页的内容，通过StringBuilder将bytes转化为char
    private static String getContent (InputStream input) throws IOException {
        String content;
        int n;
        StringBuilder sb = new StringBuilder();
        while ((n = input.read()) != -1) {
            sb.append((char) n);
        }
        content = sb.toString();
        return content;
    }
}
