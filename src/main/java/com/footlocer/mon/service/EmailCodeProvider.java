package com.footlocer.mon.service;

import com.footlocer.mon.dto.ProviderOrder;
import com.footlocer.mon.dto.ProviderResponse;

import java.util.List;

public interface EmailCodeProvider {
    /** 下单购买（单次按 quantity） */
    ProviderResponse buy(int quantity) throws Exception;

    /** 是否应该就这个错误码重试 */
    boolean shouldRetry(int code);

    /** 把 provider 的“成功条目（订单/邮箱）”统一抽出 */
    List<ProviderOrder> extractOrders(ProviderResponse resp);

    /** 抽出可能的 links 或附加信息（可为空） */
    String extractLinks(ProviderResponse resp);

    /** 供 Discord/embed 使用的“原始返回字符串” */
    String rawBody(ProviderResponse resp);
}

