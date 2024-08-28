package com.footlocer.mon.manager;


import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class GenHypeProxy {

    private static final int THREAD_POOL_SIZE = 8000;
    private static final int TIMEOUT = 500; // in milliseconds

    public String genHype() {
        Path desktopPath = Paths.get(System.getProperty("user.home"), "Desktop");
        Path filePath = desktopPath.resolve("output.txt");

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<String> proxyList = new ArrayList<>();
        //31.128.114.187:61158:gxdlndjk:rDjS6f881o
        String ip = "31.128.";
        String username = "gxdlndjk";
        String password = "rDjS6f881o";

        for (int duan = 112; duan < 127; duan++) {
            for (int i = 0; i < 256; i++) {
                AtomicBoolean found = new AtomicBoolean(false);

                for (int port = 49200; port < 66000 && !found.get(); port++) {
                    String proxyAddress = ip + duan + "." + i + ":" + port + username+ ":" + password;

                    int finalPort = port;
                    int finalDuan = duan;
                    int finalI = i;
                    executor.submit(() -> {
                        if (isProxyWorking(ip + finalDuan + "." + finalI, finalPort, username, password)) {
                            synchronized (proxyList) {
                                proxyList.add(proxyAddress);
                            }
                            System.out.println(proxyAddress);
                            found.set(true);
                        }
                    });
                }
            }
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 将所有可用的代理地址写入文件
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            for (String proxy : proxyList) {
                writer.write(proxy);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean isProxyWorking(String proxyIp, int proxyPort, String user, String password) {
        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIp, proxyPort));
            URL url = new URL("http://www.google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);

            String encoded = Base64.getEncoder().encodeToString((user + ":" + password).getBytes("UTF-8"));
            connection.setRequestProperty("Proxy-Authorization", "Basic " + encoded);

            connection.connect();
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode == 200;
        } catch (IOException e) {
            return false;
        }
    }
}

