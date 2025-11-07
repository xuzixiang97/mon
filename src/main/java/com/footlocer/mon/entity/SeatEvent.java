package com.footlocer.mon.entity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 表示一次“监控到的座位”事件。
 * - 所有字段尽量与日志里的键对应（id/floor/floorNo/area/seatNo/block/blockNo/seatGradeName/price 等）
 * - raw 保存原始块文本，方便排障或二次解析
 */
public class SeatEvent {

    // 事件发生时间（从日志行解析到的时间字符串；可能是带时区的 ISO 字符串）
    private String ts;

    // 关键字段（按你的日志键位命名）
    private String id;
    private String floor;       // 原始 "floor"（如：2층）
    private String floorNo;     // 原始 "floorNo"（如：2）
    private String area;        // 原始 "area"（如：30）
    private String seatNo;      // 原始 "seatNo"（如：67）
    private String block;       // 原始 "block"
    private String blockNo;     // 原始 "blockNo"
    private String seatGrade;   // 原始 "seatGradeName"
    private String price;       // 原始 "price"

    // 原始块全文
    private String raw;

    // 你可能还想附带的扩展键值（非必需）
    private Map<String, Object> extra = new LinkedHashMap<String, Object>();

    public SeatEvent() {}

    // —— 常用显示字段 —— //
    /** 楼层用于展示时的优先级：floorNo > floor */
    public String displayFloor() {
        if (notEmpty(floorNo)) return floorNo;
        if (notEmpty(floor))   return floor;
        return "N/A";
    }

    /** 分区用于展示时的优先级：area > block > blockNo */
    public String displayBlock() {
        if (notEmpty(area))    return area;
        if (notEmpty(block))   return block;
        if (notEmpty(blockNo)) return blockNo;
        return "N/A";
    }

    /** 展示用的 “座位号” */
    public String displaySeat() {
        return notEmpty(seatNo) ? seatNo : "N/A";
    }

    // —— 指纹：用于去重 —— //
    public String fingerprint() {
        String base = (displayFloor() + "|" + displayBlock() + "|" + displaySeat() + "|" + snip(raw, 80))
                .toLowerCase();
        return base;
    }

    private static boolean notEmpty(String s) { return s != null && !s.trim().isEmpty(); }
    private static String snip(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : (s.substring(0, Math.max(0, max - 3)) + "...");
    }

    // —— Getter/Setter —— //
    public String getTs() { return ts; }
    public void setTs(String ts) { this.ts = ts; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }
    public String getFloorNo() { return floorNo; }
    public void setFloorNo(String floorNo) { this.floorNo = floorNo; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getSeatNo() { return seatNo; }
    public void setSeatNo(String seatNo) { this.seatNo = seatNo; }
    public String getBlock() { return block; }
    public void setBlock(String block) { this.block = block; }
    public String getBlockNo() { return blockNo; }
    public void setBlockNo(String blockNo) { this.blockNo = blockNo; }
    public String getSeatGrade() { return seatGrade; }
    public void setSeatGrade(String seatGrade) { this.seatGrade = seatGrade; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getRaw() { return raw; }
    public void setRaw(String raw) { this.raw = raw; }
    public Map<String, Object> getExtra() { return extra; }
    public void setExtra(Map<String, Object> extra) { this.extra = extra; }
}
