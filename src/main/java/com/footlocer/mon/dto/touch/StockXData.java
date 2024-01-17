package com.footlocer.mon.dto.touch;

import lombok.Data;

@Data
public class StockXData {
    private double size;
    private int StockX;
    //价格 按照这个算就好
    private int price;
    private String sourceSize;
    private int salesAmount;
    private int priceUS;

    // Getters and setters for each field
}