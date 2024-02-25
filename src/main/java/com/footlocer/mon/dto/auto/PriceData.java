package com.footlocer.mon.dto.auto;

public class PriceData {
    private String msg;
    private Prices prices;

    // Constructor, getters, and setters
    public PriceData() {
    }

    public PriceData(String msg, Prices prices) {
        this.msg = msg;
        this.prices = prices;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Prices getPrices() {
        return prices;
    }

    public void setPrices(Prices prices) {
        this.prices = prices;
    }
}


