package com.footlocer.mon.dto.auto;

import lombok.Data;
import org.apache.commons.math3.stat.descriptive.summary.Product;

import java.util.LinkedList;
import java.util.List;

@Data
public class ProductSellListing {

    private LinkedList<ProductSell> list;
    private int totalNum;
}
