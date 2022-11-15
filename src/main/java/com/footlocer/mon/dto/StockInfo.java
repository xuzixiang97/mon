package com.footlocer.mon.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockInfo {

    private String sku;
    private String footlockerSize;
    private String champssportSize;
    private String kidSize;
}
