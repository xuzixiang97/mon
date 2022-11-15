package com.footlocer.mon.dto;

import lombok.Data;

@Data
public class Proxy {

    private String hostname;
    private String port;
    private String authUser;
    private String authPassword;
}
