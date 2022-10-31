package com.footlocer.mon.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class XuHttpUtil {

    public static String getHtml(String url) throws IOException {

        URL url2 = new URL(url);

        // 构建传入代理连接的ip、port和用户名密码
        List<String> authList = new ArrayList<>();
        String hostname="us-30m.geosurf.io";
        String  port = "8000";
        String authUser = "626761+US+626761-333565885050";
        String authPassword = "ieYdzl";
        authList.add(0,hostname);
        authList.add(1,port);
        authList.add(2,authUser);
        authList.add(3,authPassword);

        //通过代理进行连接
        URLConnection urlConnection = url2.openConnection(getProxy(authList));
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
