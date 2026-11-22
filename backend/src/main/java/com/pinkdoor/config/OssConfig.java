package com.pinkdoor.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.pinkdoor.config.properties.OssProperties;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 客户端：整个应用共用一个，应用关闭时释放连接。
 */
@Configuration
@RequiredArgsConstructor
public class OssConfig {

    private final OssProperties ossProperties;

    private OSS ossClient;

    @Bean
    public OSS ossClient() {
        ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret());
        return ossClient;
    }

    @PreDestroy
    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
