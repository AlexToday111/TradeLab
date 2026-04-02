package com.example.back.common.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI tradeLabOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TradeLab Java API")
                        .description("API for managing strategies, datasets, candles, imports, and backtest runs.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TradeLab")
                                .email("ernestkudakaev6@mail.ru"))
                        .license(new License()
                                .name("Proprietary")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project documentation"));
    }
}
