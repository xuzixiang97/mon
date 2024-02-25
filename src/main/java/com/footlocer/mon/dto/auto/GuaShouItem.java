package com.footlocer.mon.dto.auto;

import lombok.Data;

import java.util.List;

@Data
public class GuaShouItem {

    private String _id;
    private int amount;
    private String sku;
    private String name;
    private String pictureUrl;
    private List<String> sizeDetails;
    private boolean[] stockxPartlyPause;
    private boolean[] goatPartlyPause;
}
