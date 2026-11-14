package com.pinkdoor.init;

import com.pinkdoor.config.properties.PresetAccountProperties;
import com.pinkdoor.couple.entity.Couple;
import com.pinkdoor.couple.service.CoupleService;
import com.pinkdoor.user.entity.User;
import com.pinkdoor.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 首次启动时按配置创建账号和情侣空间，之后每次启动同步空间和密码。
 * 相同 {@code couple-name} 的账号会进入同一个空间。
 * 修改暗号或空间只需改 application.yml，重启即可生效。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PresetAccountInitializer implements ApplicationRunner {

    private final PresetAccountProperties presetAccountProperties;
    private final UserService userService;
    private final CoupleService coupleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        List<PresetAccountProperties.Account> accounts = presetAccountProperties.getPresetAccounts();
        if (accounts == null || accounts.isEmpty()) {
            log.warn("Preset accounts are not configured; skipping initialization.");
            return;
        }
        accounts.forEach(this::initAccount);
    }

    /**
     * 创建或同步单个账号：先确保空间存在，再绑定账号。
     */
    private void initAccount(PresetAccountProperties.Account account) {
        String nickname = account.getNickname();
        String rawPassword = account.getPassword();
        String coupleName = account.getCoupleName();
        if (!StringUtils.hasText(nickname) || !StringUtils.hasText(rawPassword)
                || !StringUtils.hasText(coupleName)) {
            log.warn("Preset account is incomplete, nickname={}, coupleName={}", nickname, coupleName);
            return;
        }
        nickname = nickname.trim();
        Couple couple = coupleService.getOrCreate(coupleName.trim());

        User existing = userService.getByNickname(nickname);
        if (existing == null) {
            User user = new User();
            user.setCoupleId(couple.getId());
            user.setNickname(nickname);
            user.setPassword(passwordEncoder.encode(rawPassword));
            userService.save(user);
            log.info("Preset account '{}' created in couple '{}'.", nickname, coupleName);
            return;
        }
        boolean changed = false;
        if (!couple.getId().equals(existing.getCoupleId())) {
            existing.setCoupleId(couple.getId());
            changed = true;
        }
        if (!passwordEncoder.matches(rawPassword, existing.getPassword())) {
            existing.setPassword(passwordEncoder.encode(rawPassword));
            changed = true;
        }
        if (changed) {
            userService.updateById(existing);
            log.info("Preset account '{}' synced.", nickname);
        }
    }
}
