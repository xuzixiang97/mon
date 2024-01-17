package com.footlocer.mon.dto.touch;

import lombok.Data;

import java.util.List;

@Data
public class Shoe {
    private String name;
    private String sku;
    private String cleanSku;
    private List<SizeOption> sizeOptions;
    private String pictureUrl;
    private int specialDisplayPriceCents;
    // 销售数量
    private int soldAmount;
    private List<StockXData> stockxData;
    private String stockxStatus;
    private List<CNData> cnData;
    private String cnStatus;

    // Getters and setters for each field
}