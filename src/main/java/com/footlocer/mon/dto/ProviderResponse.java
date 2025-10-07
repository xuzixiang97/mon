package com.footlocer.mon.dto;

import lombok.Data;
import java.util.*;

@Data
public class ProviderResponse {
    private int code;            // 规范化后的状态码
    private String message;      // 规范化后的信息
    private String raw;          // 原始 JSON 字符串
    private Map<String,Object> data;  // 通用 data（可选）
}

