package com.footlocer.mon.dto.auto;

import lombok.Data;

@Data
public class BasePrice {

    //货号  不带-
    private String sku;

    private String size;

    private Integer stockxLow;

    private Integer goatLow;
}
