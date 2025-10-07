package com.footlocer.mon.manager;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.footlocer.mon.util.BuyResponse; // 若你没有该DTO，请见本文末尾附录
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class ItpMonitor {

    private static final String URL = "https://api.online-disposablemail.com/api/mailbox";
    private static final String API_KEY = "d0cf5b457aa946ef8c0e40bea4cf229e";

    // 购买目标与批次
    private static final int TARGET_TOTAL = 500;   // 目标总量
    private static final int BATCH_SIZE   = 100;   // 每批数量

    // 超时 & 重试
    private static final int REQ_TIMEOUT_MS         = 5_000;  // 请求超时
    private static final int RETRY_INTERVAL_MS      = 5_000;  // 失败后等待再试
    private static final int MAX_ATTEMPTS_PER_BATCH = 3600;    // 单批最大尝试次数

    // 业务参数
    private static final int  SERVICE_ID    = 51;   // Cityline
    private static final int  EMAIL_TYPE_ID = 3;
    private static final int  BUY_MODE      = 0;
    private static final boolean LINK_PRIORITY = true;

    // ✅ 成功 & 失败分开的 Discord Webhook（替换成你的）
    private static final String DISCORD_SUCCESS_WEBHOOK = "https://discord.com/api/webhooks/1378244046459240539/8GR94xIDsx_YrBs8YhUKvxewCfheLOtjLhEEGNts971Et1mW6fWoc3qifoBvnFqq7eG7";
    private static final String DISCORD_FAILURE_WEBHOOK = "https://discord.com/api/webhooks/1421886298729353421/dIREP7DN82yo133cBOhPfls2JWMl8RT7WU7xNsH2-RG9z6r2TKhFA52pTPbMQvc6O81W";

    private final ObjectMapper mapper = new ObjectMapper();

    /** 入口：循环按批次购买，成功与失败分别走不同 webhook（Embed 样式） */
    public void monitor() {
        int purchasedTotal = 0;
        int batchIndex = 0;

        while (purchasedTotal < TARGET_TOTAL) {
            batchIndex++;
            System.out.println(String.format("==== 开始批次 #%d（本批 %d，已购 %d/%d） ====",
                    batchIndex, BATCH_SIZE, purchasedTotal, TARGET_TOTAL));

            BuyResponse resp = buyOneBatchWithRetry(BATCH_SIZE, batchIndex);
            if (resp == null) {
                // 本批最终失败：已经在每次失败/异常时发过失败 webhook，这里再发一次收尾
                sendDiscordFailure("任务终止",
                        Arrays.asList(
                                embedField("批次", "#" + batchIndex, true),
                                embedField("累计", purchasedTotal + "/" + TARGET_TOTAL, true),
                                embedField("参数", paramBlock(), false)
                        ),
                        null
                );
                return;
            }

            int got = (resp.getData() != null && resp.getData().getOrders() != null)
                    ? resp.getData().getOrders().size() : 0;
            purchasedTotal += got;

            // 本批成功：成功 webhook（含示例订单 & links）
            String desc = "本批购买成功，详情如下：";
            List<Map<String, Object>> fields = new ArrayList<Map<String, Object>>();
            fields.add(embedField("批次", "#" + batchIndex, true));
            fields.add(embedField("本批数量", String.valueOf(got), true));
            fields.add(embedField("累计", purchasedTotal + "/" + TARGET_TOTAL, true));
            fields.add(embedField("参数", paramBlock(), false));

            String preview = buildOrdersPreview(resp, 6);
            if (!preview.isEmpty()) {
                fields.add(embedField("示例订单", "```text\n" + preview + "\n```", false));
            }
            String links = (resp.getData() != null) ? safe(resp.getData().getLinks()) : "";
            if (!links.isEmpty()) {
                fields.add(embedField("links", "```text\n" + truncate(links, 900) + "\n```", false));
            }

            postDiscordEmbed(DISCORD_SUCCESS_WEBHOOK, "✅ 购买成功", desc, 0x2ECC71, fields, "ItpMonitor • " + Instant.now());

            // 达成目标即停止
            if (purchasedTotal >= TARGET_TOTAL) {
                postDiscordEmbed(DISCORD_SUCCESS_WEBHOOK,
                        "🎉 任务完成", "累计购买已达成目标。",
                        0x2ECC71,
                        Arrays.asList(embedField("累计", purchasedTotal + "/" + TARGET_TOTAL, false)),
                        "ItpMonitor • " + Instant.now());
                break;
            }
        }
    }

    /**
     * 单批购买（quantity 指定）。
     * - 每次失败或异常：立刻发“失败” webhook（Embed，带详细信息与 raw）。
     * - 成功返回 BuyResponse；超过重试上限 / 不可重试错误返回 null。
     */
    private BuyResponse buyOneBatchWithRetry(int quantity, int batchIndex) {
        int attempt = 0;
        while (attempt < MAX_ATTEMPTS_PER_BATCH) {
            attempt++;
            String raw = null;

            try {
                HttpResponse response = HttpUtil.createGet(URL)
                        .form("apiKey", API_KEY)
                        .form("serviceId", SERVICE_ID)
                        .form("emailTypeId", EMAIL_TYPE_ID)
                        .form("quantity", quantity)
                        .form("buyMode", BUY_MODE)
                        .form("linkPriority", LINK_PRIORITY)
                        .timeout(REQ_TIMEOUT_MS)
                        .execute();

                raw = response.body();
                System.out.println("[批次 " + batchIndex + " / 尝试 " + attempt + "] 返回: " + raw);

                BuyResponse br = mapper.readValue(raw, BuyResponse.class);

                if (br.getCode() == 200 && br.getData() != null && br.getData().getOrders() != null) {
                    return br; // ✅ 成功
                }

                // 失败：发失败 Embed（每次）
                sendDiscordFailureDetailed(batchIndex, attempt, br.getCode(), br.getMsg(), raw);

                // 可重试 → 等待继续
                if (shouldRetry(br.getCode())) {
                    sleepSilently(RETRY_INTERVAL_MS);
                } else {
                    return null; // 不可重试 → 结束该批
                }

            } catch (Exception e) {
                String err = e.getClass().getSimpleName() + ": " + safe(e.getMessage());
                sendDiscordFailureDetailed(batchIndex, attempt, -1, err, raw);
                sleepSilently(RETRY_INTERVAL_MS);
            }
        }

        // 达到最大尝试次数仍失败
        sendDiscordFailure("批次失败（达到重试上限）",
                Arrays.asList(
                        embedField("批次", "#" + batchIndex, true),
                        embedField("次数", String.valueOf(MAX_ATTEMPTS_PER_BATCH), true),
                        embedField("参数", paramBlock(), false)
                ),
                null
        );
        return null;
    }

    /** 哪些错误码值得重试（可按需调整） */
    private boolean shouldRetry(int code) {
        switch (code) {
            // 可重试
            case 40000: // 服务器内部错误
            case 41002: // 购买邮箱超时
            case 41003: // 邮箱库存不足
            case 42001: // 等待接取验证码中...
                return true;

            // 明确不可重试
            case 11001: // 缺少参数
            case 11002: // 请求方式错误
            case 11004: // 请求资源不存在
            case 40001: // 账户找不到
            case 40002: // 账户不可用
            case 41001: // 参数无效
            case 41004: // 账户余额不足
            case 41005: // 账户购买已上限
            case 42002: // 订单已关闭
            case 42003: // 邮箱已失效
            case 42004: // 订单未找到
            case 42005: // 任务未找到
            case 42006: // 任务超时
            case 43001: // 当前订单状态不能激活
                return false;

            default:
                // 未知错误码：保守起见先重试
                return true;
        }
    }

    /* ===================== Discord Webhook（Embed） ===================== */

    /** 失败（简要） */
    private void sendDiscordFailure(String title, List<Map<String,Object>> fields, String rawJson) {
        List<Map<String,Object>> fs = (fields == null) ? new ArrayList<Map<String, Object>>() : new ArrayList<Map<String, Object>>(fields);
        if (rawJson != null && !rawJson.isEmpty()) {
            fs.add(embedField("原始返回", "```json\n" + truncate(rawJson, 1500) + "\n```", false));
        }
        postDiscordEmbed(DISCORD_FAILURE_WEBHOOK, "❌ " + safe(title), "本次请求失败。", 0xE74C3C, fs, "ItpMonitor • " + Instant.now());
    }

    /** 失败（详细） */
    private void sendDiscordFailureDetailed(int batch, int attempt, int code, String msg, String rawJson) {
        List<Map<String,Object>> fields = new ArrayList<Map<String,Object>>();
        fields.add(embedField("批次", "#" + batch, true));
        fields.add(embedField("尝试", String.valueOf(attempt), true));
        fields.add(embedField("参数", paramBlock(), false));
        fields.add(embedField("错误码", String.valueOf(code), true));
        fields.add(embedField("错误信息", safe(msg), true));
        if (rawJson != null && !rawJson.isEmpty()) {
            fields.add(embedField("原始返回", "```json\n" + truncate(rawJson, 1500) + "\n```", false));
        }
        postDiscordEmbed(DISCORD_FAILURE_WEBHOOK, "❌ 购买失败", "本次请求失败的详细信息如下：", 0xE74C3C, fields, "ItpMonitor • " + Instant.now());
    }

    /** 发送一个 Embed （自动处理 429 简单重试一次） */
    private void postDiscordEmbed(String webhookUrl,
                                  String title,
                                  String description,
                                  int colorHex,
                                  List<Map<String,Object>> fields,
                                  String footerText) {
        try {
            Map<String,Object> embed = new HashMap<String, Object>();
            embed.put("title", truncate(title, 256));
            embed.put("description", truncate(description, 2048));
            embed.put("color", colorHex);
            embed.put("timestamp", Instant.now().toString());
            if (fields != null && !fields.isEmpty()) {
                embed.put("fields", fields);
            }
            if (footerText != null && !footerText.isEmpty()) {
                Map<String,Object> footer = new HashMap<String, Object>();
                footer.put("text", truncate(footerText, 2048));
                embed.put("footer", footer);
            }

            Map<String,Object> payload = new HashMap<String, Object>();
            payload.put("embeds", Collections.singletonList(embed));

            String json = mapper.writeValueAsString(payload);

            HttpResponse r = HttpUtil.createPost(webhookUrl)
                    .header("Content-Type", "application/json")
                    .body(json)
                    .timeout(REQ_TIMEOUT_MS)
                    .execute();

            int status = r.getStatus();
            String body = r.body();
            System.out.println("📣 Webhook HTTP " + status + " 响应: " + body);

            if (status == 429) {
                String retryAfter = r.header("Retry-After");
                long waitMs = 2000;
                try { waitMs = (long) (Double.parseDouble(retryAfter) * 1000); } catch (Exception ignored) {}
                sleepSilently(waitMs);

                HttpResponse r2 = HttpUtil.createPost(webhookUrl)
                        .header("Content-Type", "application/json")
                        .body(json)
                        .timeout(REQ_TIMEOUT_MS)
                        .execute();
                System.out.println("📣 Webhook 重试: HTTP " + r2.getStatus() + " " + r2.body());
            }
        } catch (Exception e) {
            System.out.println("⚠️ 发送 Webhook 失败: " + e.getMessage());
        }
    }

    /* ===================== 小工具 ===================== */

    private Map<String,Object> embedField(String name, String value, boolean inline) {
        Map<String,Object> f = new HashMap<String, Object>();
        f.put("name", truncate(name, 256));
        f.put("value", truncate(value, 1024)); // Discord 单个 field 的 value 最长 1024
        f.put("inline", inline);
        return f;
    }

    private String paramBlock() {
        return "serviceId=" + SERVICE_ID +
                "\nemailTypeId=" + EMAIL_TYPE_ID +
                "\nquantity=" + BATCH_SIZE;
    }

    private String buildOrdersPreview(BuyResponse br, int limit) {
        if (br == null || br.getData() == null || br.getData().getOrders() == null) return "";
        List<BuyResponse.Order> orders = br.getData().getOrders();
        StringBuilder sb = new StringBuilder();
        int n = Math.min(limit, orders.size());
        for (int i = 0; i < n; i++) {
            BuyResponse.Order o = orders.get(i);
            sb.append(o.getOrderId()).append(" | ").append(o.getEmail()).append("\n");
        }
        if (orders.size() > n) {
            sb.append("... 共 ").append(orders.size()).append(" 条");
        }
        return sb.toString();
    }

    private void sleepSilently(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max) + " …";
    }
}
