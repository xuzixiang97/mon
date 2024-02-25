package com.footlocer.mon.dto.auto;

import lombok.Data;
import org.apache.commons.math3.stat.descriptive.summary.Product;

import java.util.List;

@Data
public class ProductSellListing {

    private List<ProductSell> list;
    private int totalNum;
}
