package com.footlocer.mon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mailbuyer")
public class MonitorProps {

    private int targetTotal;
    private int batchSize;
    private int reqTimeoutMs;
    private int retryIntervalMs;
    private int maxAttemptsPerBatch;
    private String discordSuccessWebhook;
    private String discordFailureWebhook;
    private String provider;

    private CitylineProps cityline = new CitylineProps();
    private Gmail500Props gmail500 = new Gmail500Props();

    // getters/setters ...

    public static class CitylineProps {
        private String baseUrl;
        private String apiKey;
        private int serviceId;
        private int emailTypeId;
        private int buyMode;
        private boolean linkPriority;
        // getters/setters ...
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public int getServiceId() { return serviceId; }
        public void setServiceId(int serviceId) { this.serviceId = serviceId; }
        public int getEmailTypeId() { return emailTypeId; }
        public void setEmailTypeId(int emailTypeId) { this.emailTypeId = emailTypeId; }
        public int getBuyMode() { return buyMode; }
        public void setBuyMode(int buyMode) { this.buyMode = buyMode; }
        public boolean isLinkPriority() { return linkPriority; }
        public void setLinkPriority(boolean linkPriority) { this.linkPriority = linkPriority; }
    }

    public static class Gmail500Props {
        private String baseUrl;
        private String apiKey;
        private int serviceId;
        private int emailTypeId;
        private int buyMode;
        private boolean linkPriority;
        // getters/setters ...
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public int getServiceId() { return serviceId; }
        public void setServiceId(int serviceId) { this.serviceId = serviceId; }
        public int getEmailTypeId() { return emailTypeId; }
        public void setEmailTypeId(int emailTypeId) { this.emailTypeId = emailTypeId; }
        public int getBuyMode() { return buyMode; }
        public void setBuyMode(int buyMode) { this.buyMode = buyMode; }
        public boolean isLinkPriority() { return linkPriority; }
        public void setLinkPriority(boolean linkPriority) { this.linkPriority = linkPriority; }
    }

    // getters/setters for top-level fields
    public int getTargetTotal() { return targetTotal; }
    public void setTargetTotal(int targetTotal) { this.targetTotal = targetTotal; }
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    public int getReqTimeoutMs() { return reqTimeoutMs; }
    public void setReqTimeoutMs(int reqTimeoutMs) { this.reqTimeoutMs = reqTimeoutMs; }
    public int getRetryIntervalMs() { return retryIntervalMs; }
    public void setRetryIntervalMs(int retryIntervalMs) { this.retryIntervalMs = retryIntervalMs; }
    public int getMaxAttemptsPerBatch() { return maxAttemptsPerBatch; }
    public void setMaxAttemptsPerBatch(int maxAttemptsPerBatch) { this.maxAttemptsPerBatch = maxAttemptsPerBatch; }
    public String getDiscordSuccessWebhook() { return discordSuccessWebhook; }
    public void setDiscordSuccessWebhook(String discordSuccessWebhook) { this.discordSuccessWebhook = discordSuccessWebhook; }
    public String getDiscordFailureWebhook() { return discordFailureWebhook; }
    public void setDiscordFailureWebhook(String discordFailureWebhook) { this.discordFailureWebhook = discordFailureWebhook; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public CitylineProps getCityline() { return cityline; }
    public void setCityline(CitylineProps cityline) { this.cityline = cityline; }

    public Gmail500Props getGmail500() { return gmail500; }
    public void setGmail500(Gmail500Props gmail500) { this.gmail500 = gmail500; }
}
