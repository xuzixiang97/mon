package com.footlocer.mon.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BuyResponse {
    private int code;
    private String msg;
    private Data data;
    // getter/setter

    @JsonIgnoreProperties(ignoreUnknown = true)
    @lombok.Data
    public static class Data {
        private List<Order> orders;
        private String links;
        // getter/setter
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Order {
        private String orderId;
        private String email;
        // getter/setter
    }
}