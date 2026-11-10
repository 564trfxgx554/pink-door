package com.pinkdoor.init;

import com.pinkdoor.config.properties.PresetAccountProperties;
import com.pinkdoor.user.entity.User;
import com.pinkdoor.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 首次启动时创建唯一的预置账号，之后每次启动都将密码哈希与
 * {@code pink-door.preset-account.password} 保持同步。
 * 修改暗号只需改 application.yml（或 PRESET_PASSWORD），重启即可生效。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PresetAccountInitializer implements ApplicationRunner {

    private final PresetAccountProperties presetAccountProperties;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        String nickname = presetAccountProperties.getNickname();
        String rawPassword = presetAccountProperties.getPassword();
        if (!StringUtils.hasText(nickname) || !StringUtils.hasText(rawPassword)) {
            log.warn("Preset account is not configured; skipping initialization.");
            return;
        }
        nickname = nickname.trim();

        User existing = userService.getByNickname(nickname);
        if (existing == null) {
            User user = new User();
            user.setNickname(nickname);
            user.setPassword(passwordEncoder.encode(rawPassword));
            userService.save(user);
            log.info("Preset account '{}' created.", nickname);
            return;
        }
        if (!passwordEncoder.matches(rawPassword, existing.getPassword())) {
            existing.setPassword(passwordEncoder.encode(rawPassword));
            userService.updateById(existing);
            log.info("Preset account '{}' password synced from config.", nickname);
        }
    }
}
