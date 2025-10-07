package com.footlocer.mon.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mailbuyer.gmail500")
@Data
public class Gmail500Props {
    private String baseUrl;
    private String apiKey;
    private Integer serviceId;
    private Integer emailTypeId;
    private Integer buyMode;
    private boolean linkPriority;

    // 也复用全局，但保个单独可覆盖
    private Integer reqTimeoutMs = 5000;
}
