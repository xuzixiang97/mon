package com.footlocer.mon.dto.auto;

public class Prices {
private String stockxLowestAsk;
private String stockxHighestBid;
private int goatStv;
private int goatInstant;
private int ebayPrice;

// Constructor, getters, and setters
public Prices() {
        }

public Prices(String stockxLowestAsk, String stockxHighestBid, int goatStv, int goatInstant, int ebayPrice) {
        this.stockxLowestAsk = stockxLowestAsk;
        this.stockxHighestBid = stockxHighestBid;
        this.goatStv = goatStv;
        this.goatInstant = goatInstant;
        this.ebayPrice = ebayPrice;
        }

public String getStockxLowestAsk() {
        return stockxLowestAsk;
        }

public void setStockxLowestAsk(String stockxLowestAsk) {
        this.stockxLowestAsk = stockxLowestAsk;
        }

public String getStockxHighestBid() {
        return stockxHighestBid;
        }

public void setStockxHighestBid(String stockxHighestBid) {
        this.stockxHighestBid = stockxHighestBid;
        }

public int getGoatStv() {
        return goatStv;
        }

public void setGoatStv(int goatStv) {
        this.goatStv = goatStv;
        }

public int getGoatInstant() {
        return goatInstant;
        }

public void setGoatInstant(int goatInstant) {
        this.goatInstant = goatInstant;
        }

public int getEbayPrice() {
        return ebayPrice;
        }

public void setEbayPrice(int ebayPrice) {
        this.ebayPrice = ebayPrice;
        }
        }