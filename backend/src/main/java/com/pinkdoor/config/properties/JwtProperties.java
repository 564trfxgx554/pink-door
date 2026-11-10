package com.pinkdoor.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置，对应 {@code pink-door.jwt}。
 */
@Data
@ConfigurationProperties(prefix = "pink-door.jwt")
public class JwtProperties {

    /**
     * Base64 编码的 HMAC 密钥，解码后至少 32 字节。
     */
    private String secret;

    private long expireHours = 24;
}
