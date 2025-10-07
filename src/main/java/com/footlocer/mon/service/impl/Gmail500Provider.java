package com.footlocer.mon.service.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.footlocer.mon.config.MonitorProps;
import com.footlocer.mon.dto.ProviderOrder;
import com.footlocer.mon.dto.ProviderResponse;
import com.footlocer.mon.service.EmailCodeProvider;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Gmail500 邮箱验证码购买接口
 * 适配 GET: https://emailapi.info/openapi/v2/mail/code/buy?apiKey=xxx&productCode=1000
 * 每条成功订单会实时写入 logs/gmail500_success.csv
 */
@Component("gmail500Provider")
public class Gmail500Provider implements EmailCodeProvider {

    private final ObjectMapper mapper = new ObjectMapper();
    private final MonitorProps.Gmail500Props props;
    private final MonitorProps top; // 拿全局配置

    private final File logFile;

    public Gmail500Provider(MonitorProps monitorProps) {
        this.props = monitorProps.getGmail500();
        this.top = monitorProps;

        // 日志文件路径
        File dir = new File("logs");
        if (!dir.exists()) dir.mkdirs();
        this.logFile = new File(dir, "gmail500_success.csv");

        // 初始化表头
        if (!logFile.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
                pw.println("time,orderNo,address,url");
            } catch (IOException e) {
                System.out.println("⚠️ 无法创建日志文件: " + e.getMessage());
            }
        }
    }

    @Override
    public ProviderResponse buy(int quantity) throws Exception {
        List<ProviderOrder> allOrders = new ArrayList<ProviderOrder>();
        StringBuilder allLinks = new StringBuilder();

        String lastRaw = "";
        String lastMsg = "";
        int lastCode = -1;

        for (int i = 0; i < Math.max(1, quantity); i++) {
            HttpResponse r = HttpUtil.createGet(props.getBaseUrl())
                    .form("apiKey", props.getApiKey())
                    .form("productCode", props.getProductCode())
                    .timeout(5000)
                    .execute();

            String raw = r.body();
            lastRaw = raw;

            Map<String, Object> map = mapper.readValue(raw, new TypeReference<Map<String, Object>>() {});
            boolean successful = Boolean.TRUE.equals(map.get("successful"));
            Number codeNum = (Number) map.get("code");
            int apiCode = codeNum == null ? -1 : codeNum.intValue();

            lastCode = (successful && apiCode == 0) ? 200 : apiCode;
            lastMsg = String.valueOf(map.get("msg"));

            if (successful && apiCode == 0 && map.get("data") instanceof Map) {
                Map data = (Map) map.get("data");
                String orderNo = String.valueOf(data.get("orderNo"));
                Object detailObj = data.get("orderDetail");
                if (detailObj == null) detailObj = data.get("orderDetails");

                String address = "";
                String url = "";
                if (detailObj instanceof Map) {
                    Map detail = (Map) detailObj;
                    Object addrObj = detail.get("address");
                    Object urlObj = detail.get("url");
                    address = addrObj == null ? "" : String.valueOf(addrObj);
                    url = urlObj == null ? "" : String.valueOf(urlObj);
                }

                ProviderOrder order = new ProviderOrder();
                order.setOrderId(orderNo);
                order.setEmail(address);
                allOrders.add(order);

                if (url != null && !url.isEmpty()) {
                    allLinks.append(url).append("\n");
                }

                // ✅ 实时记录
                recordToFile(orderNo, address, url);
            } else {
                System.out.println("❌ Gmail500 返回失败: " + lastMsg + " | " + raw);
            }
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("orders", allOrders);
        data.put("links", allLinks.toString().trim());

        ProviderResponse resp = new ProviderResponse();
        resp.setRaw(lastRaw);
        resp.setCode(lastCode);
        resp.setMessage(lastMsg);
        resp.setData(data);
        return resp;
    }

    @Override
    public boolean shouldRetry(int code) {
        return code != 200; // 200即成功
    }

    @Override
    public List<ProviderOrder> extractOrders(ProviderResponse resp) {
        if (resp.getData() == null) return Collections.emptyList();
        Object obj = resp.getData().get("orders");
        if (!(obj instanceof List)) return Collections.emptyList();
        return (List<ProviderOrder>) obj;
    }

    @Override
    public String extractLinks(ProviderResponse resp) {
        if (resp.getData() == null) return "";
        Object links = resp.getData().get("links");
        return links == null ? "" : String.valueOf(links);
    }

    @Override
    public String rawBody(ProviderResponse resp) {
        return resp == null ? "" : resp.getRaw();
    }

    /* ====================== 文件写入 ====================== */

    private void recordToFile(String orderNo, String address, String url) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            pw.printf("%s,%s,%s,%s%n",
                    escapeCsv(time),
                    escapeCsv(orderNo),
                    escapeCsv(address),
                    escapeCsv(url));
        } catch (IOException e) {
            System.out.println("⚠️ 写入日志失败: " + e.getMessage());
        }

        // 控制台日志
        System.out.println("[✅ 成功] orderNo=" + orderNo + " | " + address + " | " + url);
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        String value = s.replace("\"", "\"\"");
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = "\"" + value + "\"";
        }
        return value;
    }
}
