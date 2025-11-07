package com.footlocer.mon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "seat.monitor")
public class SeatMonitorProperties {
    private String logFile;
    private String webhook;
    private int periodSec = 30;
    private int dedupSize = 2000;
    private long retryMs = 1200L;

    /** 每次读取的时间窗口（分钟），默认近 60 分钟 */
    private int windowMinutes = 120;

    /** 从文件尾部读取的最大大小（MB），默认 8MB，避免整文件读取 */
    private int tailMb = 8;

    // getters / setters
    public String getLogFile() { return logFile; }
    public void setLogFile(String logFile) { this.logFile = logFile; }
    public String getWebhook() { return webhook; }
    public void setWebhook(String webhook) { this.webhook = webhook; }
    public int getPeriodSec() { return periodSec; }
    public void setPeriodSec(int periodSec) { this.periodSec = periodSec; }
    public int getDedupSize() { return dedupSize; }
    public void setDedupSize(int dedupSize) { this.dedupSize = dedupSize; }
    public long getRetryMs() { return retryMs; }
    public void setRetryMs(long retryMs) { this.retryMs = retryMs; }

    public int getWindowMinutes() { return windowMinutes; }
    public void setWindowMinutes(int windowMinutes) { this.windowMinutes = windowMinutes; }

    public int getTailMb() { return tailMb; }
    public void setTailMb(int tailMb) { this.tailMb = tailMb; }
}