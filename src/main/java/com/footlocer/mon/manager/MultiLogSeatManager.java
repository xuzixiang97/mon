package com.footlocer.mon.manager;

import com.footlocer.mon.entity.SeatEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
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
 * 管理层（只改 Manager，不动 SeatMonitorService）：
 *
 * 目标：
 * 1) 接收一组日志文件路径（可运行时设置/追加）
 * 2) 按固定周期轮询（本实现：每 10s；如需 30s 改 @Scheduled 即可）
 * 3) 对每个文件：调用 service.readRecentHolds(file, tailMbPerFile) 只读尾部一定大小（MB）
 * 4) 汇总所有文件读到的 SeatEvent，做二次处理：
 *    - 时间过滤：仅保留最近 windowMinutes 分钟内事件（ts 无法解析的事件保留，避免漏报）
 *    - 按“座位（楼层/区/座）”去重：同一座位仅保留时间最新的一条
 * 5) 控制台输出最终结果；如需推送 Discord，在末尾调用 service.pushToDiscord(finalList)
 *
 * 说明：
 * - 本类聚焦“调度与整合”，不解析日志文本，解析由 SeatMonitorService 负责。
 * - 为了排障，增加了 tailContainsKeyword 的轻探针，用于判断尾部是否包含关键关键字。
 */
@Component
@EnableScheduling
public class MultiLogSeatManager {

    /** 解析日志、发送 Discord 的 service（保持不变，由外部注入） */
    private final SeatMonitorService service;

    /** 被轮询的日志文件路径列表（绝对/相对路径均可） */
    private final List<String> logFiles = new ArrayList<>();

    /** 过滤窗口：仅保留最近 N 分钟的事件（默认 100 分钟） */
    private volatile int windowMinutes = 100;

    /** 每次从文件尾部读取的大小（单位：MB；默认 10MB） */
    private volatile int tailMbPerFile = 10;

    /** 北京时间格式化，仅用于打印展示 */
    private static final DateTimeFormatter BJ_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));

    public MultiLogSeatManager(SeatMonitorService service) {
        this.service = service;
    }

    /* ===================== 外部注入/运行期控制 ===================== */

    /**
     * 一次性设置日志文件列表（覆盖旧列表）
     * @param files 日志路径集合；允许为空（表示清空）
     */
    public synchronized void setLogFiles(Collection<String> files) {
        this.logFiles.clear();
        if (files != null) this.logFiles.addAll(files);
        System.out.println("[MultiLogSeatManager] set " + this.logFiles.size() + " log files.");
        for (String f : this.logFiles) {
            System.out.println("  - " + f);
        }
    }

    /**
     * 运行期追加一个日志文件
     * @param file 日志路径（空串忽略）
     */
    public synchronized void addLogFile(String file) {
        if (file != null && !file.trim().isEmpty()) {
            this.logFiles.add(file.trim());
            System.out.println("[MultiLogSeatManager] add log file: " + file);
        }
    }

    /** 设置时间过滤窗口（分钟） */
    public void setWindowMinutes(int minutes) {
        if (minutes > 0) this.windowMinutes = minutes;
    }

    /** 设置每次从文件尾部读取的大小（MB） */
    public void setTailMbPerFile(int mb) {
        if (mb > 0) this.tailMbPerFile = mb;
    }

    /* ========================= 定时任务 ========================= */

    /**
     * 调度入口：固定延迟（上次执行完毕后等待 10s 再启动）
     * - 如需固定频率或 30s 周期，改注解即可：@Scheduled(fixedDelay = 30_000L)
     */
    @Scheduled(fixedDelay = 10_000L)
    public void tick() {
        // 打印调度时间（北京时间）
        System.out.println("[MultiLogSeatManager] tick @ " + BJ_FMT.format(Instant.now()) + " [Asia/Shanghai]");

        // 读取快照，避免遍历时并发修改
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
                // 调用保持不变的 service；注意：这里的 tail 参数是“MB”
                List<SeatEvent> list = service.readRecentHolds(f, tailMbPerFile);
                int got = (list == null) ? 0 : list.size();
                System.out.println("  • readRecentHolds(" + f.getName() + ", tail=" + tailMbPerFile + "MB) -> " + got);

                if (got == 0) {
                    // 若没有解析到块，做一次 1MB 的轻探针：是否包含关键字“try to hold seat: [”
                    boolean hasTryHold = tailContainsKeyword(
                            f, 1, "(?i)try\\s+(to\\s+)?hold\\s+seat\\s*:\\s*\\["
                    );
                    System.out.println("    └─ tail probe (1MB) contains 'try to hold seat:[' ? " + hasTryHold);

                    // 如果严格关键字也没有，再做一个宽松关键字探测，辅助判断“日志根本没产生”还是“解析规则没命中”
                    if (!hasTryHold) {
                        boolean hasLoose = tailContainsKeyword(f, 1, "(?i)hold\\s+seat");
                        System.out.println("       (loose probe 'hold seat' ? " + hasLoose + ")");
                    }
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

        // key = floor|block|seat（统一小写；空缺以 "na" 占位）
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
