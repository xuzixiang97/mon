package com.footlocer.mon.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/** 顶层响应 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceResponse {
    private int code;
    private String msg;
    private List<Service> data;

    /** data 数组元素 */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Service {
        // 注意：返回是字符串，所以用 String，避免类型转换问题
        private String serviceId;
        private String serviceName;
        private List<ServiceItem> items;
    }

    /** items 数组元素 */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServiceItem {
        private String serviceItemId;
        private String serviceItemName;
        private String senderName; // 新增这个字段的接收
    }
}

