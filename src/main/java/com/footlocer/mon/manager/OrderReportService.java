package com.footlocer.mon.manager;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * 拉取所有批次 -> 拉取每批次 links -> 解析 orderId/email -> 导出 CSV
 * 需要携带登录态：Authorization Bearer + Cookie
 */
@Service
public class OrderReportService {

    private static final String BASE = "https://online-disposablemail.com";
    private static final String LIST_API  = BASE + "/api/order/batch/list";
    private static final String LINKS_API = BASE + "/api/order/batch/links/"; // + {batchId}

    private static final int TIMEOUT_MS = 10_000;

    private final ObjectMapper mapper = new ObjectMapper();

    /** 对外入口：把所有订单导出为 CSV */
    public void exportAllOrdersToCsv(String outCsvPath, String bearerToken, String cookie) {
        try {
            // 1) 拉取所有批次（分页，带鉴权）
            List<BatchItem> allBatches = fetchAllBatches(1, 100, bearerToken, cookie);

            // 2) 遍历批次，拉 links 并解析
            List<OrderRow> rows = new ArrayList<>();
            for (BatchItem b : allBatches) {
                String links = fetchLinks(b.getId(), bearerToken, cookie);
                rows.addAll(expandOrders(b, links));
            }

            // 3) 写 CSV
            writeCsv(rows, outCsvPath);
            System.out.println("✅ 导出完成，共 " + rows.size() + " 条 -> " + outCsvPath);
        } catch (Exception e) {
            System.out.println("❌ 导出失败: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    /* ========== HTTP 调用（带头） ========== */

    private List<BatchItem> fetchAllBatches(int startPage, int pageSize, String bearerToken, String cookie) throws Exception {
        int pageNum = startPage;
        boolean hasNext = true;
        List<BatchItem> all = new ArrayList<>();

        while (hasNext) {
            HttpResponse resp = HttpUtil.createGet(LIST_API)
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Cookie", cookie)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .form("pageNum", pageNum)
                    .form("pageSize", pageSize)
                    .timeout(TIMEOUT_MS)
                    .execute();

            String body = resp.body();
            BatchListResponse r = mapper.readValue(body, BatchListResponse.class);
            if (r.getCode() != 200) {
                throw new IllegalStateException("list api code=" + r.getCode() + " msg=" + r.getMsg());
            }

            if (r.getList() != null) all.addAll(r.getList());
            hasNext = r.isNext();
            pageNum++;
        }
        return all;
    }

    private String fetchLinks(String batchId, String bearerToken, String cookie) throws Exception {
        HttpResponse resp = HttpUtil.createGet(LINKS_API + batchId)
                .header("Authorization", "Bearer " + bearerToken)
                .header("Cookie", cookie)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT_MS)
                .execute();

        String body = resp.body();
        LinksResponse r = mapper.readValue(body, LinksResponse.class);
        if (r.getCode() != 200) {
            throw new IllegalStateException("links api code=" + r.getCode() + " msg=" + r.getMsg());
        }
        return r.getData(); // 多行 "…orderId=XXXX----email"
    }

    /* ========== 解析 & 导出 ========== */

    private List<OrderRow> expandOrders(BatchItem b, String linksData) {
        List<OrderRow> rows = new ArrayList<>();
        if (linksData == null || linksData.trim().isEmpty()) return rows;

        String[] lines = linksData.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            String orderId = "";
            String email   = "";

            int idx = trimmed.indexOf("orderId=");
            if (idx >= 0) {
                String right = trimmed.substring(idx + "orderId=".length());
                String[] pair = right.split("----", 2);
                if (pair.length == 2) {
                    orderId = pair[0].trim();
                    email   = pair[1].trim();
                }
            }

            OrderRow row = new OrderRow();
            row.batchId         = b.getId();
            row.createTime      = b.getCreateTime();
            row.serviceName     = b.getServiceName();
            row.emailTypeName   = b.getEmailTypeName();
            row.batchQuantity   = b.getQuantity();
            row.batchTotalAmount= b.getTotalAmount();
            row.orderId         = orderId;
            row.email           = email;
            row.linkUrl         = trimmed;
            rows.add(row);
        }
        return rows;
    }

    private void writeCsv(List<OrderRow> rows, String outCsvPath) throws Exception {
        Path p = Paths.get(outCsvPath);
        if (p.getParent() != null) Files.createDirectories(p.getParent());

        try (BufferedWriter w = Files.newBufferedWriter(
                p, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            w.write("batchId,createTime,serviceName,emailTypeName,batchQuantity,batchTotalAmount,orderId,email,linkUrl");
            w.newLine();

            for (OrderRow r : rows) {
                w.write(csv(r.batchId)); w.write(',');
                w.write(csv(r.createTime)); w.write(',');
                w.write(csv(r.serviceName)); w.write(',');
                w.write(csv(r.emailTypeName)); w.write(',');
                w.write(csv(String.valueOf(r.batchQuantity))); w.write(',');
                w.write(csv(r.batchTotalAmount == null ? "" : r.batchTotalAmount.toPlainString())); w.write(',');
                w.write(csv(r.orderId)); w.write(',');
                w.write(csv(r.email)); w.write(',');
                w.write(csv(r.linkUrl));
                w.newLine();
            }
            w.flush();
        }
    }

    private static String csv(String s) {
        if (s == null) return "";
        boolean need = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String out = s.replace("\"", "\"\"");
        return need ? "\""+out+"\"" : out;
    }

    /* ========== DTOs ========== */

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchListResponse {
        private int code;
        private String msg;
        private List<BatchItem> list;
        private int total;
        private int totalPage;
        private int pageSize;
        private int page;
        private boolean next;
        private boolean prev;

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        public String getMsg() { return msg; }
        public void setMsg(String msg) { this.msg = msg; }
        public List<BatchItem> getList() { return list; }
        public void setList(List<BatchItem> list) { this.list = list; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public int getTotalPage() { return totalPage; }
        public void setTotalPage(int totalPage) { this.totalPage = totalPage; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public boolean isNext() { return next; }
        public void setNext(boolean next) { this.next = next; }
        public boolean isPrev() { return prev; }
        public void setPrev(boolean prev) { this.prev = prev; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchItem {
        private String id;
        private String serviceName;
        private String emailTypeId;
        private String emailTypeName;
        private int quantity;
        private BigDecimal totalAmount;
        private String createTime;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getEmailTypeId() { return emailTypeId; }
        public void setEmailTypeId(String emailTypeId) { this.emailTypeId = emailTypeId; }
        public String getEmailTypeName() { return emailTypeName; }
        public void setEmailTypeName(String emailTypeName) { this.emailTypeName = emailTypeName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public String getCreateTime() { return createTime; }
        public void setCreateTime(String createTime) { this.createTime = createTime; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LinksResponse {
        private int code;
        private String msg;
        private String data;

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        public String getMsg() { return msg; }
        public void setMsg(String msg) { this.msg = msg; }
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    public static class OrderRow {
        public String batchId;
        public String createTime;
        public String serviceName;
        public String emailTypeName;
        public int    batchQuantity;
        public BigDecimal batchTotalAmount;
        public String orderId;
        public String email;
        public String linkUrl;
    }
}
