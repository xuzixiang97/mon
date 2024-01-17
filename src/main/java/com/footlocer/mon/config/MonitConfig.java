package com.footlocer.mon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitConfig {

    @Bean
    @ConfigurationProperties("touch")
    public TouchProperties touchProperties() {
        return new TouchProperties();
    }

}
