package com.pinkdoor.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pinkdoor.user.entity.User;

/**
 * 用户领域服务。
 */
public interface UserService extends IService<User> {

    /**
     * 按昵称查询账号。
     *
     * @param nickname 登录昵称
     * @return 用户；不存在时返回 {@code null}
     */
    User getByNickname(String nickname);
}
