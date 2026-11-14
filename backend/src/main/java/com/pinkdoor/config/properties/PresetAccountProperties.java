package com.pinkdoor.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 预置账号配置，对应 {@code pink-door.preset-accounts}。
 * 相同 {@code couple-name} 的账号会被放进同一个情侣空间。
 */
@Data
@ConfigurationProperties(prefix = "pink-door")
public class PresetAccountProperties {

    /**
     * 预置账号列表。首次启动创建，之后每次启动与配置同步。
     */
    private List<Account> presetAccounts = new ArrayList<>();

    /**
     * 单个预置账号。
     */
    @Data
    public static class Account {

        private String nickname;

        private String password;

        /**
         * 空间名称，作为初始化时的空间标识。
         */
        private String coupleName;
    }
}
