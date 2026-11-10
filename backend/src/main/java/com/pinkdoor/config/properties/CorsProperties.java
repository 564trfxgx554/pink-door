package com.pinkdoor.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 跨域配置，对应 {@code pink-door.cors}。
 */
@Data
@ConfigurationProperties(prefix = "pink-door.cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:5173"));
}
