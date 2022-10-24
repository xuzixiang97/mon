package com.footlocer.mon.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.net.www.http.HttpClient;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/hellow")
public class HelloController {

    @GetMapping("/test")
    public String hello() {
        return "aaa";
    }

    @GetMapping("/test1")
    public String hello1() throws IOException {

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1080));

//        Document document = Jsoup.connect("https://www.footlocker.com/en/product/~/J5423500.html")
//                .proxy(proxy)
//                .get();
//
        URL url = new URL("https://www.footlocker.com/en/product/~/J5423500.html");

        // 构建传入代理连接的ip、port和用户名密码
        List<String> authList = new ArrayList<>();
        String hostname="101.49.178.11";
        String  port = "11089";
        String authUser = "xuxuImail0715";
        String authPassword = "5YMlQ4lU";
        authList.add(0,hostname);
        authList.add(1,port);
        authList.add(2,authUser);
        authList.add(3,authPassword);

        //通过代理进行连接
        URLConnection urlConnection = url.openConnection(getProxy(authList));

        //构建input输入流
        InputStream input = urlConnection.getInputStream();

        //将内容打印至控制台
        System.out.println(getContent(input));



        return "aaa";
    }




    //设置Proxy并返回
    public static  Proxy getProxy(List<String> authList){

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
    public static String getContent (InputStream input) throws IOException {
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
