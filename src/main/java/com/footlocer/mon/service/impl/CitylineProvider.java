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

import java.util.*;

/**
 * Cityline 邮箱购买服务提供者
 * 使用 MonitorProps 自动注入配置（兼容 JDK8）
 */
@Component("citylineProvider")
public class CitylineProvider implements EmailCodeProvider {

    private final ObjectMapper mapper = new ObjectMapper();
    private final MonitorProps.CitylineProps props;

    public CitylineProvider(MonitorProps monitorProps) {
        this.props = monitorProps.getCityline();
    }

    @Override
    public ProviderResponse buy(int quantity) throws Exception {
        // 执行 API 调用
        HttpResponse r = HttpUtil.createGet(props.getBaseUrl())
                .form("apiKey", props.getApiKey())
                .form("serviceId", props.getServiceId())
                .form("emailTypeId", props.getEmailTypeId())
                .form("quantity", quantity)
                .form("buyMode", props.getBuyMode())
                .form("linkPriority", props.isLinkPriority())
                .timeout(5000)
                .execute();

        String raw = r.body();
        Map<String, Object> map = mapper.readValue(raw, new TypeReference<Map<String, Object>>() {});

        ProviderResponse resp = new ProviderResponse();
        resp.setRaw(raw);
        resp.setCode(((Number) map.getOrDefault("code", -1)).intValue());
        resp.setMessage(String.valueOf(map.getOrDefault("msg", "")));

        if (map.containsKey("data") && map.get("data") instanceof Map) {
            resp.setData((Map<String, Object>) map.get("data"));
        }

        return resp;
    }

    @Override
    public boolean shouldRetry(int code) {
        switch (code) {
            case 40000: // 服务器内部错误
            case 41002: // 购买邮箱超时
            case 41003: // 邮箱库存不足
            case 42001: // 等待接取验证码中
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<ProviderOrder> extractOrders(ProviderResponse resp) {
        if (resp.getData() == null) return Collections.emptyList();

        Object obj = resp.getData().get("orders");
        if (!(obj instanceof List)) return Collections.emptyList();

        List<Map<String, Object>> list = (List<Map<String, Object>>) obj;
        List<ProviderOrder> result = new ArrayList<ProviderOrder>();

        for (Map<String, Object> m : list) {
            ProviderOrder o = new ProviderOrder();
            o.setOrderId(String.valueOf(m.get("orderId")));
            o.setEmail(String.valueOf(m.get("email")));
            result.add(o);
        }
        return result;
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
}
