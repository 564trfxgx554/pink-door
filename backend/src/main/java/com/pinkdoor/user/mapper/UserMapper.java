package com.pinkdoor.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinkdoor.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * {@link User} 的数据访问层，本模块使用 MyBatis-Plus 内置方法即可。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
