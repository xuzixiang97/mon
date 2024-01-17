package com.footlocer.mon.dto.touch;

import lombok.Data;

@Data
public class CNData {
    private String size;
    //普通发货价格 按照这个算
    private int ptPrice;
    private int plusPrice;
    private int jsPrice;
    private String sizeUS;

    // Getters and setters for each field
}
