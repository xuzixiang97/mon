package com.footlocer.mon.dto.touch;

import lombok.Data;

import java.util.List;

@Data
public class TouchList {
    private List<Shoe> list;

    public List<Shoe> getList() {
        return list;
    }

    public void setList(List<Shoe> list) {
        this.list = list;
    }
}








