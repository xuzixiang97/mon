package com.footlocer.mon.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.footlocer.mon.config.MonitorProps;
import com.footlocer.mon.dto.ProviderOrder;
import com.footlocer.mon.dto.ProviderResponse;
import com.footlocer.mon.service.EmailCodeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * 通用邮箱购买监控器
 * 自动根据配置选择 Provider（gmail500 / cityline）
 * 完全兼容 JDK 8
 */
@Service
public class MailBuyerMonitor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final EmailCodeProvider provider;
    private final MonitorProps props;

    @Autowired
    public MailBuyerMonitor(List<EmailCodeProvider> providers, MonitorProps props) {
        this.props = props;
        String which = props.getProvider() == null ? "" : props.getProvider().toLowerCase();

        EmailCodeProvider chosen = null;
        for (EmailCodeProvider p : providers) {
            String name = p.getClass().getSimpleName().toLowerCase();
            if (name.contains(which)) {
                chosen = p;
                break;
            }
        }

        this.provider = chosen != null ? chosen : providers.get(0);
        System.out.println("👉 当前使用的接口实现：" + provider.getClass().getSimpleName());
    }

    /** 主执行逻辑 */
    public void monitor() {
        int purchasedTotal = 0;
        int batchIndex = 0;

        while (purchasedTotal < props.getTargetTotal() && !Thread.currentThread().isInterrupted()) {
            batchIndex++;
            int quantity = props.getBatchSize();

            ProviderResponse resp = buyOneBatchWithRetry(quantity, batchIndex);
            if (resp == null) {
                sendFailure("任务终止", batchIndex, purchasedTotal, provider.rawBody(null));
                break;
            }

            List<ProviderOrder> orders = provider.extractOrders(resp);
            int got = orders.size();
            purchasedTotal += got;

            List<Map<String, Object>> fields = new ArrayList<Map<String, Object>>();
            fields.add(embed("批次", "#" + batchIndex, true));
            fields.add(embed("本批数量", String.valueOf(got), true));
            fields.add(embed("累计", purchasedTotal + "/" + props.getTargetTotal(), true));

            String preview = buildPreview(orders, 6);
            if (!preview.isEmpty()) {
                fields.add(embed("示例订单", "```text\n" + preview + "\n```", false));
            }

            String links = provider.extractLinks(resp);
            if (links != null && !links.isEmpty()) {
                fields.add(embed("links", "```text\n" + truncate(links, 900) + "\n```", false));
            }

            postDiscord(props.getDiscordSuccessWebhook(),
                    "✅ 购买成功", "本批购买成功", 0x2ECC71, fields);

            // 达标停止
            if (purchasedTotal >= props.getTargetTotal()) {
                postDiscord(props.getDiscordSuccessWebhook(),
                        "🎉 任务完成", "累计购买已达成目标。",
                        0x2ECC71,
                        Collections.singletonList(embed("累计",
                                purchasedTotal + "/" + props.getTargetTotal(), false)));
                break;
            }
        }

        System.out.println("🟢 Monitor 结束时间：" + Instant.now());
    }

    /** 批量购买 + 重试 */
    private ProviderResponse buyOneBatchWithRetry(int quantity, int batchIndex) {
        int attempt = 0;

        while (attempt < props.getMaxAttemptsPerBatch() && !Thread.currentThread().isInterrupted()) {
            attempt++;
            ProviderResponse resp = null;
            String raw = null;

            try {
                resp = provider.buy(quantity);
                raw = resp.getRaw();

                if (resp.getCode() == 200 && !provider.extractOrders(resp).isEmpty()) {
                    return resp; // ✅ 成功
                }

                sendFailureDetailed(batchIndex, attempt, resp.getCode(), resp.getMessage(), raw);

                if (!provider.shouldRetry(resp.getCode())) {
                    return null;
                }

                sleep(backoff(attempt, props.getRetryIntervalMs(), 120_000));

            } catch (Exception e) {
                sendFailureDetailed(batchIndex, attempt, -1,
                        e.getClass().getSimpleName() + ": " + safe(e.getMessage()), raw);
                sleep(backoff(attempt, props.getRetryIntervalMs(), 120_000));
            }
        }

        sendFailure("批次失败（达到重试上限或被中断）", batchIndex, 0, null);
        return null;
    }

    // ====== 公共工具方法 ======

    private String buildPreview(List<ProviderOrder> orders, int limit) {
        if (orders == null || orders.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        int n = Math.min(limit, orders.size());
        for (int i = 0; i < n; i++) {
            ProviderOrder o = orders.get(i);
            sb.append(o.getOrderId()).append(" | ").append(o.getEmail()).append("\n");
        }
        if (orders.size() > n) {
            sb.append("... 共 ").append(orders.size()).append(" 条");
        }
        return sb.toString();
    }

    private Map<String, Object> embed(String name, String value, boolean inline) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", truncate(name, 256));
        map.put("value", truncate(value, 1024));
        map.put("inline", inline);
        return map;
    }

    private void postDiscord(String webhook, String title, String desc, int color, List<Map<String, Object>> fields) {
        try {
            Map<String, Object> embed = new HashMap<String, Object>();
            embed.put("title", truncate(title, 256));
            embed.put("description", truncate(desc, 2048));
            embed.put("color", color);
            embed.put("timestamp", Instant.now().toString());
            if (fields != null && !fields.isEmpty()) embed.put("fields", fields);

            Map<String, Object> payload = new HashMap<String, Object>();
            payload.put("embeds", Collections.singletonList(embed));

            String json = mapper.writeValueAsString(payload);
            cn.hutool.http.HttpResponse r = cn.hutool.http.HttpUtil.createPost(webhook)
                    .header("Content-Type", "application/json")
                    .body(json)
                    .timeout(props.getReqTimeoutMs())
                    .execute();

            System.out.println("📣 Webhook HTTP " + r.getStatus() + " " + r.body());
        } catch (Exception e) {
            System.out.println("⚠️ 发送 Discord 失败: " + e.getMessage());
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max) + " …";
    }

    private long backoff(int attempt, int baseMs, int maxMs) {
        double exp = Math.min(maxMs, baseMs * Math.pow(2, Math.min(attempt, 10)));
        double jitter = exp * (0.2 * Math.random());
        return (long) Math.min(maxMs, exp + jitter);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendFailure(String title, int batchIndex, int purchasedTotal, String raw) {
        List<Map<String, Object>> fields = new ArrayList<Map<String, Object>>();
        fields.add(embed("批次", "#" + batchIndex, true));
        fields.add(embed("累计", String.valueOf(purchasedTotal), true));
        if (raw != null && !raw.isEmpty()) {
            fields.add(embed("原始返回", "```json\n" + truncate(raw, 1500) + "\n```", false));
        }
        postDiscord(props.getDiscordFailureWebhook(), "❌ " + title, "请求失败", 0xE74C3C, fields);
    }

    private void sendFailureDetailed(int batchIndex, int attempt, int code, String msg, String raw) {
        List<Map<String, Object>> fields = new ArrayList<Map<String, Object>>();
        fields.add(embed("批次", "#" + batchIndex, true));
        fields.add(embed("尝试", String.valueOf(attempt), true));
        fields.add(embed("错误码", String.valueOf(code), true));
        fields.add(embed("错误信息", safe(msg), false));
        if (raw != null && !raw.isEmpty()) {
            fields.add(embed("原始返回", "```json\n" + truncate(raw, 1500) + "\n```", false));
        }
        postDiscord(props.getDiscordFailureWebhook(), "❌ 购买失败", "详细错误如下：", 0xE74C3C, fields);
    }
}
