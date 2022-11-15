package com.footlocer.mon.util;

import cn.hutool.core.util.StrUtil;
import com.footlocer.mon.dto.Proxy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ProxyUtil {

    /**
     * 解析代理组
     * @param proxyList
     * @return
     */
    public static List<Proxy> excuteProxyList(List<String> proxyList) {
        ////us-30m.geosurf.io:8000:626761+US+626761-548256714878:ieYdzl
        ArrayList<Proxy> list = new ArrayList<>();
        for (String proxy : proxyList) {
            List<String> split = StrUtil.split(proxy, ":");
            Proxy proxy1 = new Proxy();
            proxy1.setHostname(split.get(0));
            proxy1.setPort(split.get(1));
            proxy1.setAuthUser(split.get(2));
            proxy1.setAuthPassword(split.get(3));
            list.add(proxy1);
        }
        return list;
    }


    /**
     * 解析单条代理
     * @param proxy
     * @return
     */
    public static Proxy excuteProxy(String proxy) {
        ////us-30m.geosurf.io:8000:626761+US+626761-548256714878:ieYdzl

        List<String> split = StrUtil.split(proxy, ":");
        Proxy proxy1 = new Proxy();
        proxy1.setHostname(split.get(0));
        proxy1.setPort(split.get(1));
        proxy1.setAuthUser(split.get(2));
        proxy1.setAuthPassword(split.get(3));

        return proxy1;
    }



    public static List<String> getFileContext(String path) throws Exception {
        FileReader fileReader = new FileReader(path);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> list = new ArrayList<String>();
        String str = null;
        while ((str = bufferedReader.readLine()) != null) {
            if (str.trim().length() > 2) {
                list.add(str);
            }
        }
        return list;
    }



}
