package com.pinkdoor.ai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 调用 Python AI 服务的 WebClient。
 */
@Configuration
@RequiredArgsConstructor
public class AiClientConfig {

    private final AiProperties aiProperties;

    @Bean
    public WebClient aiWebClient() {
        return WebClient.builder()
                .baseUrl(aiProperties.getBaseUrl())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }
}
