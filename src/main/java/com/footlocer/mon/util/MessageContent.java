package com.footlocer.mon.util;

import lombok.Data;

@Data
public class MessageContent {

    /**
     * 文本内容
     */
    private String content;

    public MessageContent() {
    }

    public MessageContent(String content) {
        this.content = content;
    }

}