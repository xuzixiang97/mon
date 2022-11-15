package com.footlocer.mon.util;

import lombok.Data;

@Data
public class MessageText {

    /**
     * 消息文本类型 目前只支持文本
     */
    private String msgtype;

    /**
     * 文本消息
     */
    private MessageContent text;
}

