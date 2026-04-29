package com.example.back.livetrading.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LiveTradingProperties.class)
public class LiveTradingConfig {
}
