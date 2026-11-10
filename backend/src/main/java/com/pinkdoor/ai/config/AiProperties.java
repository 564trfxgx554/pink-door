package com.pinkdoor.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 服务配置，对应 {@code pink-door.ai}。
 */
@Data
@ConfigurationProperties(prefix = "pink-door.ai")
public class AiProperties {

    private String baseUrl = "http://127.0.0.1:8000";

    private String internalToken = "change-me";

    private long timeoutSeconds = 120;
}
