package com.pinkdoor.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pinkdoor.user.entity.User;
import com.pinkdoor.user.mapper.UserMapper;
import com.pinkdoor.user.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户领域服务实现。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getByNickname(String nickname) {
        return lambdaQuery()
                .eq(User::getNickname, nickname)
                .one();
    }
}
