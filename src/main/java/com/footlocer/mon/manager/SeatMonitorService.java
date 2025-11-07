package com.footlocer.mon.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.footlocer.mon.config.SeatMonitorProperties;
import com.footlocer.mon.entity.SeatEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 读取日志 → 解析 SeatEvent 列表（支持传入文件路径/列表），
 * 推送到 Discord（仍使用 props 中的 webhook）。
 *
 * 关键点：
 * - 只读文件尾部（默认 10MB），不做时间过滤。
 * - 每个 “try to hold seat: [ ... ]” 块解析成一个 SeatEvent。
 * - LRU 去重，防止重复推送。
 */
@Service
public class SeatMonitorService {

    private final SeatMonitorProperties props;
    private final ObjectMapper mapper = new ObjectMapper();

    public SeatMonitorService(SeatMonitorProperties props) {
        this.props = props;
    }

    // —— 正则/时间 —— //
    private static final Pattern TS_PREFIX = Pattern.compile(
            "^(?<ts>\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}(?:[.,]\\d{3})?(?:Z|[+-]\\d{2}:?\\d{2})?)\\s*(?<rest>.*)$");
    private static final Pattern P_HOLD_BEGIN =
            Pattern.compile(".*try to hold seat:\\s*\\[\\s*$", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    // —— 块状态（每次读取时使用；方法里会重置） —— //
    private boolean inHoldBlock = false;
    private StringBuilder holdBuf = new StringBuilder();
    private String holdBlockTs = null;

    // —— LRU 去重（跨批推送时生效）—— //
    private final LinkedHashMap<String, Boolean> dedup =
            new LinkedHashMap<String, Boolean>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return this.size() > Math.max(200, props.getDedupSize());
                }
            };

    @PostConstruct
    public void init() {
        System.out.println("[SeatMonitor] service ready. webhook=" + props.getWebhook());
    }

    /* =========================
     *         外部接口
     * ========================= */

    /** 从单个“文件路径”读取最近 10MB，解析所有 try-hold 块为 List<SeatEvent>（不做时间过滤） */
    public List<SeatEvent> readRecentHolds(String filePath) {
        return readRecentHolds(new File(filePath), 10L * 1024L * 1024L);
    }

    /** 从单个 File 读取最近 10MB，解析所有 try-hold 块为 List<SeatEvent>（不做时间过滤） */
    public List<SeatEvent> readRecentHolds(File file) {
        return readRecentHolds(file, 10L * 1024L * 1024L);
    }

    // 便捷：按 MB 传参
    public List<SeatEvent> readRecentHolds(File file, int tailMb) {
        long tailBytes = Math.max(1, tailMb) * 1024L * 1024L;
        return readRecentHolds(file, tailBytes);
    }

    /** 从单个 File 读取“tailBytes”尾部（字节数），解析所有 try-hold 块为 List<SeatEvent>（不做时间过滤） */
    public List<SeatEvent> readRecentHolds(File file, long tailBytes) {
        List<SeatEvent> out = new ArrayList<SeatEvent>();
        if (file == null || !file.exists() || !file.isFile()) return out;

        RandomAccessFile raf = null;
        FileChannel ch = null;

        // 仅用于解析时间戳：记录“最近一次在行首解析到的时间串”
        String lastSeenTs = null;

        try {
            raf = new RandomAccessFile(file, "r");
            ch = raf.getChannel();

            long size = ch.size();
            if (size <= 0) return out;

            long start = Math.max(0, size - Math.max(1, tailBytes));
            raf.seek(start);

            // 读入内存
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int read;
            while ((read = raf.read(buf)) > 0) {
                baos.write(buf, 0, read);
            }

            // 解码（容错）
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
            String chunk = decoder.decode(ByteBuffer.wrap(baos.toByteArray())).toString();

            // 每次读取都重置块状态，避免跨次粘连
            inHoldBlock = false;
            holdBuf.setLength(0);
            holdBlockTs = null;

            String[] lines = chunk.split("\\R");
            for (String rawLine : lines) {
                if (rawLine == null || rawLine.isEmpty()) continue;

                String tsStr = null;     // 不再用“当前时间”兜底
                String content = rawLine;

                // 行首尝试解析时间戳
                Matcher tsM = TS_PREFIX.matcher(rawLine);
                if (tsM.matches()) {
                    tsStr = tsM.group("ts");
                    content = tsM.group("rest");
                    lastSeenTs = tsStr;  // 记住最近一次解析到的时间
                }

                if (!inHoldBlock) {
                    // 识别块起始
                    if (P_HOLD_BEGIN.matcher(content).matches() || content.contains("try to hold seat: [")) {
                        inHoldBlock = true;
                        holdBuf.setLength(0);
                        // 块时间优先用当前行解析到的 ts；否则用上一行的时间；都没有就先记 null
                        holdBlockTs = (tsStr != null ? tsStr : lastSeenTs);
                        holdBuf.append(rawLine).append("\n");
                    }
                } else {
                    holdBuf.append(rawLine).append("\n");
                    String trimmed = rawLine.trim();
                    // 结束条件：行以 ']' 结束（兼容 "]" / "...]"）
                    if (trimmed.equals("]") || trimmed.startsWith("]") || trimmed.endsWith("]")) {
                        inHoldBlock = false;
                        String blockText = holdBuf.toString();

                        // 如果仍然没有时间戳，标记为 "UNKNOWN"
                        String tsForBlock = (holdBlockTs != null ? holdBlockTs : "UNKNOWN");

                        SeatEvent evt = parseHoldBlock(blockText, tsForBlock);
                        if (evt != null) out.add(evt);

                        // 重置块状态
                        holdBuf.setLength(0);
                        holdBlockTs = null;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[SeatMonitor] read error: " + e.getMessage());
        } finally {
            try { if (ch != null) ch.close(); } catch (Exception ignore) {}
            try { if (raf != null) raf.close(); } catch (Exception ignore) {}
        }

        return out;
    }

    /** 同时读取多个文件（每个文件只读尾部 10MB），合并结果后返回（按文件顺序聚合；不去重） */
    public List<SeatEvent> readRecentHolds(List<String> filePaths) {
        List<SeatEvent> all = new ArrayList<SeatEvent>();
        if (filePaths == null) return all;
        for (String p : filePaths) {
            if (p == null || p.trim().isEmpty()) continue;
            all.addAll(readRecentHolds(p.trim()));
        }
        return all;
    }

    /**
     * 把一批 SeatEvent 推送到 Discord（带 LRU 去重）。
     * - 新增字段：北京时间
     */
    public void pushToDiscord(List<SeatEvent> events) {
        if (events == null || events.isEmpty()) return;
        if (!StringUtils.hasText(props.getWebhook())) {
            System.err.println("[Discord] webhook empty, skip.");
            return;
        }

        for (SeatEvent e : events) {
            String finger = e.fingerprint();
            synchronized (dedup) {
                if (dedup.containsKey(finger)) {
                    // System.out.println("[SeatMonitor] DUP skip: " + finger);
                    continue;
                }
                dedup.put(finger, Boolean.TRUE);
            }

            // 构造 Discord payload（Hutool 发送）
            Map<String, Object> embed = new LinkedHashMap<String, Object>();
            embed.put("title", safeLimit("🎯 监控到座位（try to hold seat）", 256));
            embed.put("description", codeBlock(safeLimit(e.getRaw(), 3800)));
            embed.put("timestamp", Instant.now().toString());
            embed.put("color", 0x2ecc71);

            List<Map<String, Object>> fields = new ArrayList<Map<String, Object>>();
            fields.add(field("时间", e.getTs(), true));
            // 🆕 新增：北京时间
            fields.add(field("北京时间", toBeijing(e.getTs()), true));

            fields.add(field("楼层/FloorNo", e.displayFloor(), true));
            fields.add(field("区/Area(Block)", e.displayBlock(), true));
            fields.add(field("座位/Seat", e.displaySeat(), true));
            if (notEmpty(e.getSeatGrade())) fields.add(field("票档/Grade", e.getSeatGrade(), true));
            if (notEmpty(e.getPrice()))     fields.add(field("价格/Price", e.getPrice(), true));
            if (notEmpty(e.getBlockNo()))   fields.add(field("BlockNo", e.getBlockNo(), true));
            if (notEmpty(e.getId()))        fields.add(field("ID", e.getId(), false));
            if (!fields.isEmpty()) embed.put("fields", fields);

            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("content", "🎫 监控到座位（hold 尝试）");
            payload.put("embeds", Collections.singletonList(embed));

            try {
                String json = mapper.writeValueAsString(payload);
                String url = withWait(props.getWebhook());

                cn.hutool.http.HttpResponse r = cn.hutool.http.HttpUtil.createPost(url)
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json, text/plain, */*")
                        .header("User-Agent", "SeatMonitor/1.0")
                        .body(json)
                        .timeout(30_000) // 30s
                        .execute();

                System.out.println("📣 Discord HTTP " + r.getStatus() + " body=" + r.body());

            } catch (Exception ex) {
                System.out.println("⚠️ 发送 Discord 失败: " + ex.getMessage());
            }
        }
    }

    /** 把 ts（可能是 ISO/Z/带偏移/本地无偏移/UNKNOWN）格式化成“北京时间 yyyy-MM-dd HH:mm:ss” */
    private static String toBeijing(String ts) {
        ZoneId sh = ZoneId.of("Asia/Shanghai");
        try {
            if (ts == null || ts.trim().isEmpty() || "UNKNOWN".equalsIgnoreCase(ts.trim())) {
                return fmtInZone(Instant.now(), sh);
            }
            // 1) 优先按 OffsetDateTime（能处理 +08:00/-08:00 等偏移）
            try {
                java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(ts.trim());
                return fmtInZone(odt.toInstant(), sh);
            } catch (Exception ignore) {}
            // 2) 其次按 Instant（仅 Z 结尾）
            try {
                Instant ins = Instant.parse(ts.trim());
                return fmtInZone(ins, sh);
            } catch (Exception ignore) {}
            // 3) 兜底：本地无时区时间（例如 "yyyy-MM-dd HH:mm:ss[.SSS]"）按系统时区解释
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSS]");
            java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(ts.trim(), f);
            Instant ins = ldt.atZone(ZoneId.systemDefault()).toInstant();
            return fmtInZone(ins, sh);
        } catch (Exception e) {
            // 最终兜底
            return fmtInZone(Instant.now(), sh);
        }
    }

    private static String fmtInZone(Instant ins, ZoneId zone) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zone);
        return fmt.format(ins);
    }

    /* =========================
     *       解析 & 工具
     * ========================= */

    // 解析一个 hold 块为 SeatEvent
    private SeatEvent parseHoldBlock(String blockText, String ts) {
        SeatEvent e = new SeatEvent();
        e.setTs(ts != null ? ts : "UNKNOWN");
        e.setRaw(blockText);

        // 针对 "key: value" 形式抽取常见键
        e.setId(findKV(blockText, "id"));
        e.setFloor(findKV(blockText, "floor"));
        e.setFloorNo(findKV(blockText, "floorNo"));
        e.setArea(findKV(blockText, "area"));
        e.setSeatNo(findKV(blockText, "seatNo"));
        e.setBlock(findKV(blockText, "block"));
        e.setBlockNo(findKV(blockText, "blockNo"));
        e.setSeatGrade(findKV(blockText, "seatGradeName"));
        e.setPrice(findKV(blockText, "price"));

        return e;
    }

    private static String withWait(String url) {
        if (url == null) return null;
        return url + (url.contains("?") ? "&" : "?") + "wait=true";
    }

    private static Map<String, Object> field(String name, String value, boolean inline) {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("name", safeLimit(name, 256));
        m.put("value", safeLimit((value == null ? "N/A" : value), 1024));
        m.put("inline", inline);
        return m;
    }
    private static String codeBlock(String s) { return "```\n" + (s == null ? "" : s) + "\n```"; }
    private static boolean notEmpty(String s) { return s != null && !s.trim().isEmpty(); }
    private static String safeLimit(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : (s.substring(0, Math.max(0, max - 3)) + "...");
    }

    /** 在原文中以 key: value（容忍引号/逗号/括号等）抽取值 */
    private static String findKV(String text, String key) {
        Pattern p = Pattern.compile("\\b" + Pattern.quote(key) + "\\s*:\\s*(['\"]?)([^'\"\\n\\r\\],}]+)\\1");
        Matcher m = p.matcher(text);
        return m.find() ? m.group(2).trim() : null;
    }
}
