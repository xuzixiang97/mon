package com.footlocer.mon.manager;

import com.footlocer.mon.entity.SeatEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 管理层：负责定期从多个日志文件中读取数据，处理并去重后输出。
 *
 * 功能：
 * 1) 每 10 秒读取一组日志文件的尾部内容，调用 service.readRecentHolds(file, tailMbPerFile)
 * 2) 将所有文件读取到的 SeatEvent 数据进行二次处理：
 *    - 时间过滤：仅保留最近 N 分钟内的事件。
 *    - 按座位（楼层/区/座位）去重，保留最新时间的座位信息。
 * 3) 最终输出到控制台（包括去重前后的信息），也可以选择推送到 Discord。
 *
 * 注：SeatMonitorService 保持不变，仅本类负责调度与整合逻辑。
 */
@Component
@EnableScheduling
public class MultiLogSeatManager {

    private final SeatMonitorService service;
    private final List<String> logFiles = new ArrayList<>();
    private volatile int windowMinutes = 100;  // 默认窗口时间：100 分钟
    private volatile int tailMbPerFile = 10;  // 默认每次读取文件尾部大小：10MB

    private static final DateTimeFormatter BJ_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));

    /** 每个日志文件对应的门票日期（仅用于 Discord 展示） */
    private final Map<String, String> fileTicketDate = new HashMap<String, String>();

    /** 默认门票日期（如果某个文件没配置，则用它；可为空） */
    private volatile String defaultTicketDate = "";

    public MultiLogSeatManager(SeatMonitorService service) {
        this.service = service;
    }

    /* ===================== 外部注入/运行时控制 ===================== */

    public synchronized void setLogFiles(Collection<String> files) {
        this.logFiles.clear();
        if (files != null) this.logFiles.addAll(files);
        System.out.println("[MultiLogSeatManager] set " + this.logFiles.size() + " log files.");
    }

    /**
     * 一次性设置监控文件 + 每个文件的门票日期（覆盖旧配置）
     * @param fileToDate key=log绝对路径，value=门票日期(展示用)
     */
    public synchronized void setLogFilesWithDates(Map<String, String> fileToDate) {
        this.logFiles.clear();
        this.fileTicketDate.clear();

        if (fileToDate != null) {
            for (Map.Entry<String, String> e : fileToDate.entrySet()) {
                String path = (e.getKey() == null) ? "" : e.getKey().trim();
                if (path.isEmpty()) continue;

                this.logFiles.add(path);

                String date = (e.getValue() == null) ? "" : e.getValue().trim();
                this.fileTicketDate.put(path, date);
            }
        }

        System.out.println("[MultiLogSeatManager] set " + this.logFiles.size() + " log files with ticketDate.");
        for (String p : this.logFiles) {
            System.out.println("  - " + p + "  ticketDate=" + nvl(this.fileTicketDate.get(p), "(empty)"));
        }
    }

    /** 可选：设置默认门票日期（某些文件没配置时使用） */
    public void setDefaultTicketDate(String date) {
        this.defaultTicketDate = (date == null) ? "" : date.trim();
    }


    public synchronized void addLogFile(String file) {
        if (file != null && !file.trim().isEmpty()) {
            this.logFiles.add(file.trim());
            System.out.println("[MultiLogSeatManager] add log file: " + file);
        }
    }

    public void setWindowMinutes(int minutes) {
        if (minutes > 0) this.windowMinutes = minutes;
    }

    public void setTailMbPerFile(int mb) {
        if (mb > 0) this.tailMbPerFile = mb;
    }

    /* ========================= 定时任务：每 10 秒 ========================= */

    @Scheduled(fixedDelay = 60_000L)  // 每 10 秒执行一次
    public void tick() {
        // 打印当前调度时间（北京时间）
        System.out.println("[MultiLogSeatManager] tick @ " + BJ_FMT.format(Instant.now()) + " [Asia/Shanghai]");

        // 获取当前的文件列表快照，避免并发修改
        List<String> snapshot;
        synchronized (this) {
            snapshot = new ArrayList<>(this.logFiles);
        }
        if (snapshot.isEmpty()) {
            System.out.println("[MultiLogSeatManager] no files configured, skip.");
            return;
        }

        // 收集所有文件读到的 SeatEvent
        List<SeatEvent> merged = new ArrayList<>();

        // 逐个文件调用 service 读取尾部内容
        for (String path : snapshot) {
            File f = new File(path);
            if (!f.exists() || !f.isFile()) {
                System.err.println("[MultiLogSeatManager] skip missing file: " + path);
                continue;
            }

            try {
                // 调用保持不变的 service，读取每个文件的尾部
                List<SeatEvent> list = service.readRecentHolds(f, tailMbPerFile);
                attachTicketDate(path, list);
                int got = (list == null) ? 0 : list.size();
                //System.out.println("  • readRecentHolds(" + f.getName() + ", tail=" + tailMbPerFile + "MB) -> " + got);

                if (got == 0) {
                    // 若没有解析到块，做一次轻探针：是否包含关键字“try to hold seat: [”
                    boolean hasTryHold = tailContainsKeyword(f, 1, "(?i)try\\s+(to\\s+)?hold\\s+seat\\s*:\\s*\\[");
                    System.out.println("    └─ tail probe (1MB) contains 'try to hold seat:[' ? " + hasTryHold);
                } else {
                    merged.addAll(list);
                }
            } catch (Exception ex) {
                System.err.println("[MultiLogSeatManager] read error for " + path + ": " + ex.getMessage());
            }
        }

        // 本轮所有文件都没读到任何 SeatEvent
        if (merged.isEmpty()) {
            System.out.println("[MultiLogSeatManager] no SeatEvent collected in this tick.");
            return;
        }

        /* ----------------- 二次处理：时间过滤 ----------------- */

        // 计算时间下界：仅保留最近 windowMinutes 分钟
        long cutoff = System.currentTimeMillis() - windowMinutes * 60_000L;
        List<SeatEvent> recent = new ArrayList<>(merged.size());
        for (SeatEvent e : merged) {
            Instant t = tryParseToInstant(e.getTs());
            // 解析失败时保留：宁可误报不漏报；如需“严格过滤”，改为 (t != null && t.toEpochMilli() >= cutoff)
            if (t == null || t.toEpochMilli() >= cutoff) {
                recent.add(e);
            }
        }
        System.out.println("[MultiLogSeatManager] after time filter (" + windowMinutes + "min): " + recent.size());
        if (recent.isEmpty()) return;

        /* ----------------- 二次处理：座位去重 ----------------- */

        // 去重：只保留同一座位的最新事件
        Map<String, SeatEvent> bestBySeat = new HashMap<>();
        for (SeatEvent e : recent) {
            String key = seatKey(e);
            SeatEvent old = bestBySeat.get(key);
            // 保留时间更“新”的那一条；若新事件可解析而旧事件不可解析，则新事件更“新”
            if (old == null || isAfterOrUnknownAsNewer(e.getTs(), old.getTs())) {
                bestBySeat.put(key, e);
            }
        }

        // 转为列表并按时间降序（未知时间排最后）
        List<SeatEvent> finalList = new ArrayList<>(bestBySeat.values());
        finalList.sort(new Comparator<SeatEvent>() {
            public int compare(SeatEvent a, SeatEvent b) {
                Instant ia = tryParseToInstant(a.getTs());
                Instant ib = tryParseToInstant(b.getTs());
                if (ia == null && ib == null) return 0;
                if (ia == null) return 1;
                if (ib == null) return -1;
                return ib.compareTo(ia); // 降序
            }
        });

        /* ----------------- 输出（或转发） ----------------- */

        System.out.println("========== 最新 " + windowMinutes + " 分钟内，去重后席位共 "
                + finalList.size() + " 条 ==========");
        for (SeatEvent e : finalList) {
            Instant t = tryParseToInstant(e.getTs());
            String bj = (t == null ? "UNKNOWN" : BJ_FMT.format(t));
            System.out.println(
                    "[" + bj + "] "
                            + e.displayFloor() + " / " + e.displayBlock() + " / " + e.displaySeat()
                            + "  | grade=" + nvl(e.getSeatGrade(), "-")
                            + "  price=" + nvl(e.getPrice(), "-")
                            + "  id=" + nvl(e.getId(), "-")
            );
        }

        // 如需推送 Discord，在此处调用（SeatMonitorService 保持不变）
        service.pushToDiscord(finalList);
    }

    private void attachTicketDate(String filePath, List<SeatEvent> list) {
        if (list == null || list.isEmpty()) return;

        String td = null;
        synchronized (this) {
            td = fileTicketDate.get(filePath);
        }
        if (td == null || td.trim().isEmpty()) td = defaultTicketDate;
        if (td == null || td.trim().isEmpty()) return;

        for (SeatEvent e : list) {
            // 需要 SeatEvent 有 extra: Map<String,Object>
            Map<String, Object> extra = e.getExtra();
            if (extra == null) {
                extra = new LinkedHashMap<String, Object>();
                e.setExtra(extra);
            }
            extra.put("TicketDate", td);
        }
    }


    /* ===================== Manager 内部的轻探针 ===================== */

    /**
     * 从文件尾部读取 smallMb 大小，判断其中是否匹配给定正则。
     * 仅用于排障，不影响 service 的主流程。
     *
     * @param file    日志文件
     * @param smallMb 尾部读取大小（MB）
     * @param regex   需要匹配的正则（将被包裹在 (?s).*REGEX.* 里，以跨行匹配）
     * @return 是否匹配
     */
    private static boolean tailContainsKeyword(File file, int smallMb, String regex) {
        RandomAccessFile raf = null;
        FileChannel ch = null;
        try {
            raf = new RandomAccessFile(file, "r");
            ch = raf.getChannel();
            long size = ch.size();
            long tailBytes = Math.max(1, smallMb) * 1024L * 1024L;
            long start = Math.max(0, size - tailBytes);
            raf.seek(start);

            // 读尾部 smallMb 内容到内存
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int read;
            while ((read = raf.read(buf)) > 0) baos.write(buf, 0, read);

            // 容错解码
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
            String chunk = decoder.decode(ByteBuffer.wrap(baos.toByteArray())).toString();

            // (?s) 让 '.' 跨行匹配；两端 .* 使之成为“包含”匹配
            return chunk.matches("(?s).*" + regex + ".*");
        } catch (Exception ignore) {
            return false;
        } finally {
            try { if (ch != null) ch.close(); } catch (Exception ignore) {}
            try { if (raf != null) raf.close(); } catch (Exception ignore) {}
        }
    }

    /* ========================= 小工具方法 ========================= */

    /** 生成“座位去重”用的 key（floor|block|seat，统一小写；空值用 "na"） */
    private static String seatKey(SeatEvent e) {
        return safeLower(e.displayFloor()) + "|" + safeLower(e.displayBlock()) + "|" + safeLower(e.displaySeat());
    }

    /** 转小写并做空值兜底 */
    private static String safeLower(String s) {
        return (s == null || s.trim().isEmpty()) ? "na" : s.trim().toLowerCase(Locale.ROOT);
    }

    /** null/空串兜底 */
    private static String nvl(String s, String def) {
        return (s == null || s.isEmpty()) ? def : s;
    }

    /**
     * 尝试把 SeatEvent.ts 转为 Instant：
     * - 支持：Z、±HH:mm、±HHmm、逗号毫秒（,SSS）等常见格式
     * - 若无时区，按系统默认时区解析
     * - 解析失败返回 null
     */
    @Nullable
    private static Instant tryParseToInstant(String ts) {
        if (ts == null || ts.trim().isEmpty() || "UNKNOWN".equalsIgnoreCase(ts.trim())) return null;

        // 归一化：空格 -> 'T'；逗号毫秒 -> 点毫秒
        String t = ts.trim().replace(' ', 'T')
                .replaceFirst("(\\d{2}:\\d{2}:\\d{2}),(\\d{3})", "$1.$2");

        // +0800 -> +08:00
        if (t.matches(".*[+-]\\d{4}$")) {
            t = t.substring(0, t.length() - 5)
                    + t.substring(t.length() - 5, t.length() - 3)
                    + ":" + t.substring(t.length() - 2);
        }

        try { // 优先 OffsetDateTime（有显式偏移）
            return java.time.OffsetDateTime.parse(t).toInstant();
        } catch (Exception ignore) {}

        try { // 其次 Instant（仅适用于以 Z 结尾）
            return Instant.parse(t);
        } catch (Exception ignore) {}

        try { // 最后按本地无时区时间解析
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]");
            return java.time.LocalDateTime.parse(t, f).atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception ignore) {}

        return null;
    }

    /**
     * 判断 eTs 是否比 oldTs 更新：
     * - 两者都能解析：正常比较
     * - 仅 eTs 可解析：认为 eTs 更新（true）
     * - 仅 oldTs 可解析：认为 oldTs 更新（false）
     * - 都不可解析：退化为字符串比较（尽量保持有序）
     */
    private static boolean isAfterOrUnknownAsNewer(String eTs, String oldTs) {
        Instant a = tryParseToInstant(eTs);
        Instant b = tryParseToInstant(oldTs);
        if (a != null && b != null) return a.isAfter(b);
        if (a != null) return true;
        if (b != null) return false;
        if (eTs == null) return false;
        if (oldTs == null) return true;
        return eTs.compareTo(oldTs) >= 0;
    }
}
