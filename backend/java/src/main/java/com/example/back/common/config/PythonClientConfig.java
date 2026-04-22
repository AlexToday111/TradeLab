package com.example.back.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PythonClientConfig {

    @Value("${python.parser.base-url}")
    private String baseUrl;

    @Value("${python.parser.internal-secret}")
    private String internalSecret;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getInternalSecret() {
        return internalSecret;
    }
}
