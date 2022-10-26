package com.footlocer.mon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FootlocerBaseInfo {


    private String name;
    private String description;
    private String image;
    private String brand;
    private String model;
    private String sku;
    private String url;
    @JsonProperty("itemCondition")
    private String itemcondition;
    private List<Offers> offers;

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

    public void setImage(String image) {
        this.image = image;
    }
    public String getImage() {
        return image;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
    public String getBrand() {
        return brand;
    }

    public void setModel(String model) {
        this.model = model;
    }
    public String getModel() {
        return model;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }
    public String getSku() {
        return sku;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public String getUrl() {
        return url;
    }

    public void setItemcondition(String itemcondition) {
        this.itemcondition = itemcondition;
    }
    public String getItemcondition() {
        return itemcondition;
    }

    public void setOffers(List<Offers> offers) {
        this.offers = offers;
    }
    public List<Offers> getOffers() {
        return offers;
    }

}
