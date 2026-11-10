package com.pinkdoor.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 唯一的预置账号配置，对应 {@code pink-door.preset-account}。
 * 首次启动时创建，之后每次启动都与该配置保持同步。
 */
@Data
@ConfigurationProperties(prefix = "pink-door.preset-account")
public class PresetAccountProperties {

    private String nickname;

    private String password;
}
