package com.pinkdoor.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云 OSS 配置，对应 {@code pink-door.oss}。
 * endpoint、bucket、AK、SK 全部由环境变量提供，代码和配置文件里
 * 不写任何真实值；建议用 RAM 子账号，只给相册用的桶读写权限。
 */
@Data
@ConfigurationProperties(prefix = "pink-door.oss")
public class OssProperties {

    private String endpoint;

    private String bucket;

    private String accessKeyId;

    private String accessKeySecret;
}
