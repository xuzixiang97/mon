package com.footlocer.mon.dto.touch;

import lombok.Data;

import java.util.List;

@Data
public class ShoeExcle {

    // stockx到手价
    private String sku;

    // stockx到手价
    private double stockxHandPrice;

    //得物到手价
    private double dewuHandPrice;

    //差价
    private double priceDifference;

    //stockx三日销量
    private Integer saleAmount;


    // Getters and setters for each field
}